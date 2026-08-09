// v0.6.1: Test helper for authenticating MockMvc requests as a specific AuthUser principal.
package com.vincevscode.cointracker.support;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.security.AuthUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Shared across the *ControllerTest classes that run against the real SecurityConfig filter
 * chain (rather than {@code addFilters = false}): building a real Spring Security
 * Authentication + AuthUserDetails principal here means @PreAuthorize expressions like
 * {@code #userId == authentication.principal.userId or hasRole('ADMIN')} evaluate exactly as
 * they would in production, letting these tests assert real 200/401/403 outcomes.
 */
public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    /**
     * Pins a request's client address. {@code LoginRateLimiter} keeps a per-address budget in a
     * singleton bean, and Spring caches one application context across every slice test with the
     * same configuration — so login tests that all defaulted to 127.0.0.1 would silently spend
     * each other's budget and fail depending on execution order. Giving each test its own
     * address keeps them independent.
     */
    public static RequestPostProcessor fromAddress(String clientAddress) {
        return request -> {
            request.setRemoteAddr(clientAddress);
            return request;
        };
    }

    public static RequestPostProcessor asUser(int userId, UserRole role) {
        AuthUser authUser = new AuthUser(userId, "user" + userId, "hash", role, true, LocalDateTime.now());
        AuthUserDetails principal = new AuthUserDetails(authUser);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        return authentication(authentication);
    }
}
