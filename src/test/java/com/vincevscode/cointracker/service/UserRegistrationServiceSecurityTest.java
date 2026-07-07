// v0.7.2: Adversarial tests for UserRegistrationService (privilege escalation, input hardening).
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRegistrationServiceSecurityTest {
    private AuthUserRepositoryInterface repository;
    private PasswordEncoder passwordEncoder;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuthUserRepositoryInterface.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserRegistrationService(repository, passwordEncoder);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
    }

    @Test
    void register_shouldAlwaysCreateUserRoleNeverAdmin() {
        when(repository.findAuthUserByUsername("newbie")).thenReturn(null);
        when(repository.createAuthUser(eq("newbie"), any(), any(), anyBoolean()))
                .thenReturn(new AuthUser(5, "newbie", "hashed", UserRole.USER, true, LocalDateTime.now()));

        service.register("newbie", "password123");

        // The role is hardcoded server-side; a client can never register itself as ADMIN.
        verify(repository).createAuthUser("newbie", "hashed", UserRole.USER, true);
    }

    @Test
    void register_shouldNeverStoreThePasswordInPlainText() {
        when(repository.findAuthUserByUsername("newbie")).thenReturn(null);
        when(repository.createAuthUser(any(), any(), any(), anyBoolean()))
                .thenReturn(new AuthUser(5, "newbie", "hashed", UserRole.USER, true, LocalDateTime.now()));

        service.register("newbie", "supersecret");

        verify(passwordEncoder).encode("supersecret");
        // The stored hash is what the encoder produced, not the raw password.
        verify(repository).createAuthUser(eq("newbie"), eq("hashed"), any(), anyBoolean());
    }

    @Test
    void register_shouldRejectBlankOrWhitespaceUsername() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "password123"));
        assertThrows(IllegalArgumentException.class, () -> service.register("", "password123"));
        assertThrows(IllegalArgumentException.class, () -> service.register("   ", "password123"));
    }

    @Test
    void register_shouldRejectShortOrNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> service.register("newbie", null));
        assertThrows(IllegalArgumentException.class, () -> service.register("newbie", "short"));
        assertThrows(IllegalArgumentException.class, () -> service.register("newbie", "1234567"));
    }

    @Test
    void register_shouldRejectDuplicateUsername() {
        when(repository.findAuthUserByUsername("vince"))
                .thenReturn(new AuthUser(1, "vince", "hashed", UserRole.ADMIN, true, LocalDateTime.now()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("vince", "password123")
        );

        org.junit.jupiter.api.Assertions.assertEquals("Username is already taken.", exception.getMessage());
    }
}
