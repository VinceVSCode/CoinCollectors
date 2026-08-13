// v0.12.0: Service layer for self-service password changes.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backs {@code PATCH /api/auth/password}. The account is always the caller's own — the id comes
 * from the authenticated principal, never the request — so there is no target to authorize and
 * no way to aim this at somebody else's account.
 *
 * <p>The current password is re-checked even though the caller already holds a valid session.
 * A session is not proof of knowing the password: it can be an unattended logged-in browser or
 * a stolen cookie, and without this check either one could change the password and lock the real
 * owner out of their own account.
 */
public class PasswordChangeService {
    private final AuthUserRepositoryInterface authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordChangeService(
            AuthUserRepositoryInterface authUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @throws IllegalArgumentException if the current password is wrong or the new one fails
     *                                  {@link PasswordPolicy}.
     */
    @Transactional
    public void changePassword(int userId, String currentPassword, String newPassword) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Current password is required.");
        }

        // Validated before the current password is checked, so an obviously unusable new
        // password is rejected without spending a BCrypt comparison on the old one.
        PasswordPolicy.validate(newPassword, "New password");

        AuthUser user = authUserRepository.findAuthUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User was not found.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        authUserRepository.updatePasswordHash(userId, passwordEncoder.encode(newPassword));
    }
}
