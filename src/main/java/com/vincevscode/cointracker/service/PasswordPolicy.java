// v0.12.0: The single definition of what counts as an acceptable password.
package com.vincevscode.cointracker.service;

/**
 * Shared by {@link UserRegistrationService} and {@link PasswordChangeService}.
 *
 * <p>Kept in one place on purpose: if the two paths carried their own copies of the rule, a
 * later change to one would quietly leave the other weaker, and a strength requirement that
 * only applies at registration is no requirement at all — anyone could register with a
 * compliant password and immediately change to a one-character one.
 *
 * <p>There is no maximum here. BCrypt rejects anything over 72 bytes itself, and
 * {@code BCryptPasswordEncoder} throws {@code IllegalArgumentException} for those, which
 * {@code RestExceptionHandler} already turns into a clean 400 — verified in the pen-test pass
 * as rejected rather than silently truncated.
 */
public final class PasswordPolicy {
    // A UX/strength floor, unrelated to BCrypt's own 72-byte ceiling.
    static final int MINIMUM_PASSWORD_LENGTH = 8;

    private PasswordPolicy() {
    }

    /**
     * @param fieldLabel names the offending field so a change request can say which of its two
     *                   passwords was rejected.
     * @throws IllegalArgumentException if the password is missing or too short.
     */
    public static void validate(String password, String fieldLabel) {
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    fieldLabel + " must be at least " + MINIMUM_PASSWORD_LENGTH + " characters."
            );
        }
    }
}
