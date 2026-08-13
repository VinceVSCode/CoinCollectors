// v0.6.0: Service layer for self-service user registration.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public self-registration flow ({@code POST /api/auth/register}). Deliberately does NOT
 * accept role or active-status from the caller — every new account is hard-coded to
 * {@code UserRole.USER} + active, which forecloses a mass-assignment attack where a client
 * tries to register themselves straight in as an ADMIN (confirmed safe in the
 * {@code UserRegistrationServiceSecurityTest} pen-test pass).
 */
public class UserRegistrationService {
    private final AuthUserRepositoryInterface authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(AuthUserRepositoryInterface authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUser register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        PasswordPolicy.validate(password, "Password");

        if (authUserRepository.findAuthUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        String passwordHash = passwordEncoder.encode(password);

        try {
            return authUserRepository.createAuthUser(username, passwordHash, UserRole.USER, true);
        } catch (DataIntegrityViolationException exception) {
            // Belt-and-suspenders: the pre-check above has a race window between two concurrent
            // registrations with the same username, so also catch the DB's unique-constraint
            // violation and translate it to the same clean 400 instead of a raw 500.
            throw new IllegalArgumentException("Username is already taken.");
        }
    }
}
