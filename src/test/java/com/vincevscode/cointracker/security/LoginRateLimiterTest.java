// v0.11.0: Unit tests for login brute-force throttling.
package com.vincevscode.cointracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String ADDRESS = "10.0.0.1";
    private static final String OTHER_ADDRESS = "10.0.0.2";

    // A hand-advanced clock rather than Thread.sleep: window expiry is the whole point of this
    // class, and a test that waits 15 real minutes to prove it isn't a test anyone will run.
    private MutableClock clock;
    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        limiter = new LoginRateLimiter(3, 5, WINDOW, clock);
    }

    @Test
    void checkAllowed_shouldPermitAttemptsBelowTheThreshold() {
        limiter.recordFailure("vince", ADDRESS);
        limiter.recordFailure("vince", ADDRESS);

        assertDoesNotThrow(() -> limiter.checkAllowed("vince", ADDRESS));
    }

    @Test
    void checkAllowed_shouldBlockOnceTheUsernameThresholdIsReached() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("vince", ADDRESS)
        );
    }

    // The username budget has to follow the account, not the host, or an attacker just rotates
    // source addresses to get an unlimited number of guesses against one account.
    @Test
    void checkAllowed_shouldBlockAThrottledUsernameFromAnyAddress() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("vince", OTHER_ADDRESS)
        );
    }

    // And the address budget has to follow the host, or spraying one guess across many usernames
    // from a single machine stays completely unthrottled.
    @Test
    void checkAllowed_shouldBlockAnAddressSprayingDifferentUsernames() {
        limiter.recordFailure("alex", ADDRESS);
        limiter.recordFailure("maria", ADDRESS);
        limiter.recordFailure("dana", ADDRESS);
        limiter.recordFailure("sam", ADDRESS);
        limiter.recordFailure("kim", ADDRESS);

        assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("someone-new", ADDRESS)
        );
    }

    @Test
    void checkAllowed_shouldNotBlockAnUnrelatedUsernameFromAnUnrelatedAddress() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        assertDoesNotThrow(() -> limiter.checkAllowed("alex", OTHER_ADDRESS));
    }

    @Test
    void checkAllowed_shouldAllowAgainOnceTheWindowExpires() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        clock.advance(WINDOW.plusSeconds(1));

        assertDoesNotThrow(() -> limiter.checkAllowed("vince", ADDRESS));
    }

    @Test
    void checkAllowed_shouldReportRemainingSecondsInTheException() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        clock.advance(Duration.ofMinutes(5));

        TooManyLoginAttemptsException exception = assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("vince", ADDRESS)
        );

        assertEquals(600, exception.getRetryAfterSeconds());
    }

    // Never report "retry after 0 seconds" — a caller obeying that would be rejected again.
    @Test
    void checkAllowed_shouldReportAtLeastOneSecondAtTheEndOfTheWindow() {
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("vince", ADDRESS);
        }

        clock.advance(WINDOW.minusMillis(100));

        TooManyLoginAttemptsException exception = assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("vince", ADDRESS)
        );

        assertTrue(exception.getRetryAfterSeconds() >= 1, "was " + exception.getRetryAfterSeconds());
    }

    @Test
    void recordSuccess_shouldClearTheUsernameBudget() {
        limiter.recordFailure("vince", ADDRESS);
        limiter.recordFailure("vince", ADDRESS);

        limiter.recordSuccess("vince");
        limiter.recordFailure("vince", ADDRESS);
        limiter.recordFailure("vince", ADDRESS);

        assertDoesNotThrow(() -> limiter.checkAllowed("vince", ADDRESS));
    }

    // Otherwise an attacker holding one valid account resets their own address budget between
    // guesses simply by logging into it.
    @Test
    void recordSuccess_shouldLeaveTheAddressBudgetIntact() {
        for (int attempt = 0; attempt < 5; attempt++) {
            limiter.recordFailure("victim" + attempt, ADDRESS);
        }

        limiter.recordSuccess("attacker-own-account");

        assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("victim0", ADDRESS)
        );
    }

    // Case variations must share one budget, or "vince"/"Vince"/"VINCE" each get a fresh one.
    @Test
    void recordFailure_shouldTreatUsernameCaseInsensitively() {
        limiter.recordFailure("vince", ADDRESS);
        limiter.recordFailure("Vince", ADDRESS);
        limiter.recordFailure("VINCE", ADDRESS);

        assertThrows(
                TooManyLoginAttemptsException.class,
                () -> limiter.checkAllowed("vInCe", OTHER_ADDRESS)
        );
    }

    @Test
    void recordFailure_shouldTolerateMissingUsernameOrAddress() {
        assertDoesNotThrow(() -> limiter.recordFailure(null, null));
        assertDoesNotThrow(() -> limiter.recordFailure("  ", ""));
        assertDoesNotThrow(() -> limiter.checkAllowed(null, null));
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        private void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
