// v0.4.4: Unit tests for AuthUserQueryService validation and delegation behavior.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUserQueryServiceTest {
    private AuthUserRepositoryInterface authUserRepository;
    private AuthUserQueryService service;

    @BeforeEach
    void setUp() {
        authUserRepository = mock(AuthUserRepositoryInterface.class);
        service = new AuthUserQueryService(authUserRepository);
    }

    @Test
    void findAuthUserById_shouldReturnUserFromRepository() {
        AuthUser expectedUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        when(authUserRepository.findAuthUserById(1)).thenReturn(expectedUser);

        AuthUser foundUser = service.findAuthUserById(1);

        assertEquals(expectedUser, foundUser);
    }

    @Test
    void findAuthUserById_shouldThrowExceptionWhenIdIsNotPositive() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findAuthUserById(0)
        );

        assertEquals("User ID must be greater than 0.", exception.getMessage());
    }

    @Test
    void findAuthUserByUsername_shouldReturnUserFromRepository() {
        AuthUser expectedUser = new AuthUser(2, "alex", "hash", UserRole.USER, true, LocalDateTime.now());
        when(authUserRepository.findAuthUserByUsername("alex")).thenReturn(expectedUser);

        AuthUser foundUser = service.findAuthUserByUsername("alex");

        assertEquals(expectedUser, foundUser);
    }

    @Test
    void findAuthUserByUsername_shouldThrowExceptionWhenUsernameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findAuthUserByUsername("  ")
        );

        assertEquals("Username is required.", exception.getMessage());
    }
}
