// v0.11.0: In-memory brute-force throttling for the login endpoint.
package com.vincevscode.cointracker.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts recent failed logins per username and per client IP, and refuses further attempts once
 * either crosses its threshold within a rolling window.
 *
 * <p><strong>Both keys are tracked deliberately.</strong> Throttling only by IP does nothing
 * against credential stuffing spread across many hosts; throttling only by username lets an
 * attacker deny service to any account whose name they can guess. Tracking both closes the first
 * gap and bounds the second: windows expire on their own, so a targeted account is temporarily
 * slowed rather than locked out pending admin intervention. That residual nuisance is the
 * accepted cost of not leaving single-account brute force unthrottled.
 *
 * <p><strong>State is in-memory and per-instance.</strong> The app runs as a single container
 * (see docker-compose.yml), so a map is sufficient and avoids a Redis dependency for one
 * counter. Running more than one replica would give each its own budget, multiplying the
 * effective limit by the replica count — that is the point at which this needs shared storage.
 */
public class LoginRateLimiter {
    // Separate namespaces so a username can never collide with an IP literal.
    private static final String USERNAME_KEY_PREFIX = "u:";
    private static final String ADDRESS_KEY_PREFIX = "ip:";

    // Bounds memory against an attacker cycling random usernames to grow the map. Reaching this
    // triggers a purge of expired entries, which is enough because entries are short-lived.
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final int maxAttemptsPerUsername;
    private final int maxAttemptsPerAddress;
    private final Duration window;
    private final Clock clock;

    private final Map<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            int maxAttemptsPerUsername,
            int maxAttemptsPerAddress,
            Duration window,
            Clock clock
    ) {
        this.maxAttemptsPerUsername = maxAttemptsPerUsername;
        this.maxAttemptsPerAddress = maxAttemptsPerAddress;
        this.window = window;
        this.clock = clock;
    }

    /**
     * @throws TooManyLoginAttemptsException if either the username or the address has exhausted
     *                                       its budget for the current window.
     */
    public void checkAllowed(String username, String clientAddress) {
        long usernameRetryAfter = retryAfterSeconds(usernameKey(username), maxAttemptsPerUsername);
        long addressRetryAfter = retryAfterSeconds(addressKey(clientAddress), maxAttemptsPerAddress);
        long retryAfter = Math.max(usernameRetryAfter, addressRetryAfter);

        if (retryAfter > 0) {
            throw new TooManyLoginAttemptsException(retryAfter);
        }
    }

    /**
     * Counted for unknown usernames exactly as for real ones. Skipping the unknown case would
     * make "throttled or not" a reliable oracle for whether an account exists, undoing the
     * generic-error-message protection the login flow already relies on.
     */
    public void recordFailure(String username, String clientAddress) {
        purgeExpiredIfCrowded();

        increment(usernameKey(username));
        increment(addressKey(clientAddress));
    }

    /**
     * Clears the username's budget only. The address budget deliberately survives: an attacker
     * holding one valid account could otherwise reset their own IP allowance at will by logging
     * into it between guesses.
     */
    public void recordSuccess(String username) {
        attemptsByKey.remove(usernameKey(username));
    }

    private long retryAfterSeconds(String key, int maxAttempts) {
        if (key == null) {
            return 0;
        }

        Attempts attempts = attemptsByKey.get(key);
        Instant now = clock.instant();

        if (attempts == null || attempts.hasExpired(now, window) || attempts.count() < maxAttempts) {
            return 0;
        }

        long remaining = Duration.between(now, attempts.windowStart().plus(window)).toSeconds();

        // Round sub-second remainders up so a caller told "retry after 0" isn't sent straight
        // back into another rejection.
        return Math.max(remaining, 1);
    }

    private void increment(String key) {
        if (key == null) {
            return;
        }

        // compute() runs under the bin lock, so read-modify-write of a counter stays atomic
        // against concurrent attempts on the same key.
        attemptsByKey.compute(key, (ignoredKey, existing) -> {
            Instant now = clock.instant();

            if (existing == null || existing.hasExpired(now, window)) {
                return new Attempts(1, now);
            }

            return new Attempts(existing.count() + 1, existing.windowStart());
        });
    }

    private void purgeExpiredIfCrowded() {
        if (attemptsByKey.size() < MAX_TRACKED_KEYS) {
            return;
        }

        Instant now = clock.instant();
        attemptsByKey.entrySet().removeIf(entry -> entry.getValue().hasExpired(now, window));
    }

    private static String usernameKey(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        // Lower-cased so alternating capitalisation can't be used to get a fresh budget per
        // spelling of the same name.
        return USERNAME_KEY_PREFIX + username.trim().toLowerCase(Locale.ROOT);
    }

    private static String addressKey(String clientAddress) {
        if (clientAddress == null || clientAddress.isBlank()) {
            return null;
        }

        return ADDRESS_KEY_PREFIX + clientAddress;
    }

    private record Attempts(int count, Instant windowStart) {
        private boolean hasExpired(Instant now, Duration window) {
            return now.isAfter(windowStart.plus(window));
        }
    }
}
