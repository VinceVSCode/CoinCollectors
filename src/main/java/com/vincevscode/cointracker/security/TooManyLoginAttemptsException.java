// v0.11.0: Signals that a login attempt was throttled rather than evaluated.
package com.vincevscode.cointracker.security;

/**
 * Thrown by {@link LoginRateLimiter} before credentials are checked at all.
 *
 * <p>Deliberately not an {@code AuthenticationException}: that maps to a 401 "Invalid username or
 * password", which would be a lie here — the credentials were never evaluated — and would hide
 * from a legitimate locked-out user why their correct password appears to be failing.
 */
public class TooManyLoginAttemptsException extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(long retryAfterSeconds) {
        super("Too many login attempts. Try again in " + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
