// v0.6.0: Service layer for self-service user registration.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class UserRegistrationService {
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

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

        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MINIMUM_PASSWORD_LENGTH + " characters.");
        }

        if (authUserRepository.findAuthUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        String passwordHash = passwordEncoder.encode(password);

        try {
            return authUserRepository.createAuthUser(username, passwordHash, UserRole.USER, true);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Username is already taken.");
        }
    }
}
