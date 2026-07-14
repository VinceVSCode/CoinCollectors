// v0.4.3: Service layer for auth-focused user loading operations.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thin validation layer over {@link AuthUserRepositoryInterface} lookups. Primarily consumed
 * by {@link com.vincevscode.cointracker.security.AuthUserDetailsService} on every login/session
 * check, so keep this fast and side-effect-free (read-only transactions only).
 */
public class AuthUserQueryService {
    private final AuthUserRepositoryInterface authUserRepository;

    public AuthUserQueryService(AuthUserRepositoryInterface authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Transactional(readOnly = true)
    public AuthUser findAuthUserById(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        return authUserRepository.findAuthUserById(userId);
    }

    @Transactional(readOnly = true)
    public AuthUser findAuthUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        return authUserRepository.findAuthUserByUsername(username);
    }
}