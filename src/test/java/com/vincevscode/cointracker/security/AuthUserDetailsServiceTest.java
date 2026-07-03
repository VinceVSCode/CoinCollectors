// v0.5.1: Unit tests for AuthUserDetailsService delegation and not-found handling.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUserDetailsServiceTest {
    private AuthUserQueryService authUserQueryService;
    private AuthUserDetailsService service;

    @BeforeEach
    void setUp() {
        authUserQueryService = mock(AuthUserQueryService.class);
        service = new AuthUserDetailsService(authUserQueryService);
    }

    @Test
    void loadUserByUsername_shouldReturnAuthUserDetailsWhenUserExists() {
        AuthUser authUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        when(authUserQueryService.findAuthUserByUsername("vince")).thenReturn(authUser);

        UserDetails userDetails = service.loadUserByUsername("vince");

        assertEquals("vince", userDetails.getUsername());
        assertEquals("hash", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserDoesNotExist() {
        when(authUserQueryService.findAuthUserByUsername("nobody")).thenReturn(null);

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("nobody")
        );
    }
}
