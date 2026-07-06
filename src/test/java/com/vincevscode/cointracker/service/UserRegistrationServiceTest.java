// v0.6.0: Unit tests for UserRegistrationService validation and delegation behavior.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRegistrationServiceTest {
    private AuthUserRepositoryInterface authUserRepository;
    private PasswordEncoder passwordEncoder;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        authUserRepository = mock(AuthUserRepositoryInterface.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserRegistrationService(authUserRepository, passwordEncoder);
    }

    @Test
    void register_shouldCreateUserWithHashedPasswordAndDefaultRole() {
        when(authUserRepository.findAuthUserByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        AuthUser createdUser = new AuthUser(4, "newuser", "hashed", UserRole.USER, true, LocalDateTime.now());
        when(authUserRepository.createAuthUser("newuser", "hashed", UserRole.USER, true)).thenReturn(createdUser);

        AuthUser result = service.register("newuser", "password123");

        assertEquals(createdUser, result);
        verify(authUserRepository).createAuthUser("newuser", "hashed", UserRole.USER, true);
    }

    @Test
    void register_shouldThrowExceptionWhenUsernameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("  ", "password123")
        );

        assertEquals("Username is required.", exception.getMessage());
    }

    @Test
    void register_shouldThrowExceptionWhenPasswordIsTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("newuser", "short")
        );

        assertEquals("Password must be at least 8 characters.", exception.getMessage());
    }

    @Test
    void register_shouldThrowExceptionWhenUsernameIsAlreadyTaken() {
        AuthUser existingUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        when(authUserRepository.findAuthUserByUsername("vince")).thenReturn(existingUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("vince", "password123")
        );

        assertEquals("Username is already taken.", exception.getMessage());
    }
}
