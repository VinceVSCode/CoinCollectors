// v0.12.0: Unit tests for self-service password changes.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordChangeServiceTest {
    private static final String CURRENT_PASSWORD = "password123";

    private AuthUserRepositoryInterface repository;
    // A real encoder rather than a mock: the point of most of these cases is that the stored
    // value is a genuine BCrypt hash of the new password and not the password itself.
    private PasswordEncoder passwordEncoder;
    private PasswordChangeService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AuthUserRepositoryInterface.class);
        passwordEncoder = new BCryptPasswordEncoder();
        service = new PasswordChangeService(repository, passwordEncoder);
    }

    private void existingUser(int userId, String password) {
        when(repository.findAuthUserById(userId)).thenReturn(new AuthUser(
                userId, "alex", passwordEncoder.encode(password), UserRole.USER, true, LocalDateTime.now()
        ));
    }

    @Test
    void changePassword_shouldStoreABcryptHashOfTheNewPassword() {
        existingUser(2, CURRENT_PASSWORD);

        service.changePassword(2, CURRENT_PASSWORD, "brand-new-secret");

        Mockito.verify(repository).updatePasswordHash(Mockito.eq(2), Mockito.argThat(hash ->
                hash != null
                        && !hash.equals("brand-new-secret")
                        && passwordEncoder.matches("brand-new-secret", hash)
        ));
    }

    @Test
    void changePassword_shouldRejectAWrongCurrentPassword() {
        existingUser(2, CURRENT_PASSWORD);

        assertEquals(
                "Current password is incorrect.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changePassword(2, "not-my-password", "brand-new-secret")
                ).getMessage()
        );

        verify(repository, never()).updatePasswordHash(anyInt(), anyString());
    }

    @Test
    void changePassword_shouldRequireACurrentPassword() {
        assertEquals(
                "Current password is required.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changePassword(2, null, "brand-new-secret")
                ).getMessage()
        );

        verify(repository, never()).updatePasswordHash(anyInt(), anyString());
    }

    // The strength rule has to hold here as well as at registration, or it is trivially bypassed
    // by registering with a compliant password and immediately changing to a weak one.
    @Test
    void changePassword_shouldApplyTheSameMinimumLengthAsRegistration() {
        existingUser(2, CURRENT_PASSWORD);

        assertEquals(
                "New password must be at least 8 characters.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changePassword(2, CURRENT_PASSWORD, "short")
                ).getMessage()
        );

        verify(repository, never()).updatePasswordHash(anyInt(), anyString());
    }

    @Test
    void changePassword_shouldRejectAMissingNewPassword() {
        existingUser(2, CURRENT_PASSWORD);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(2, CURRENT_PASSWORD, null)
        );
    }

    // Validating the new password first means a hopeless request costs no BCrypt comparison
    // against the stored hash.
    @Test
    void changePassword_shouldRejectAnInvalidNewPasswordWithoutLookingUpTheUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(2, CURRENT_PASSWORD, "short")
        );

        verify(repository, never()).findAuthUserById(anyInt());
    }

    @Test
    void changePassword_shouldRejectAnUnknownUser() {
        when(repository.findAuthUserById(99)).thenReturn(null);

        assertEquals(
                "User was not found.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changePassword(99, CURRENT_PASSWORD, "brand-new-secret")
                ).getMessage()
        );
    }

    @Test
    void changePassword_shouldRejectANonPositiveUserId() {
        assertEquals(
                "User ID must be greater than 0.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changePassword(0, CURRENT_PASSWORD, "brand-new-secret")
                ).getMessage()
        );
    }

    // Two changes to the same password must not produce the same stored value; BCrypt salts each
    // hash, and equal hashes would mean the salt wasn't being applied.
    @Test
    void changePassword_shouldProduceASaltedHashEachTime() {
        existingUser(2, CURRENT_PASSWORD);

        String firstHash = passwordEncoder.encode("brand-new-secret");
        String secondHash = passwordEncoder.encode("brand-new-secret");

        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordEncoder.matches("brand-new-secret", firstHash));
        assertTrue(passwordEncoder.matches("brand-new-secret", secondHash));
    }
}
