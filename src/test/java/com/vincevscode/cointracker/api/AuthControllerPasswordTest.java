// v0.12.0: Controller tests for the self-service password change endpoint.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.PasswordChangeService;
import com.vincevscode.cointracker.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static com.vincevscode.cointracker.support.AuthTestSupport.fromAddress;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Runs the REAL filter chain so authentication and CSRF enforcement on this endpoint are
// genuinely exercised rather than assumed.
@WebMvcTest(AuthController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AuthControllerPasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordChangeService passwordChangeService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRegistrationService userRegistrationService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    private static final String BODY =
            "{\"currentPassword\":\"password123\",\"newPassword\":\"brand-new-secret\"}";

    @Test
    void changePassword_shouldDelegateForAnAuthenticatedUser() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(asUser(2, UserRole.USER))
                        .with(csrf())
                        .with(fromAddress("192.0.2.10"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk());

        verify(passwordChangeService).changePassword(2, "password123", "brand-new-secret");
    }

    // The account changed is always the principal's own. A userId smuggled into the body must
    // have no effect, or any user could repoint this at somebody else's account.
    @Test
    void changePassword_shouldIgnoreAnyUserIdInTheRequestBody() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(asUser(2, UserRole.USER))
                        .with(csrf())
                        .with(fromAddress("192.0.2.11"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"username\":\"vince\","
                                + "\"currentPassword\":\"password123\",\"newPassword\":\"brand-new-secret\"}"))
                .andExpect(status().isOk());

        // Called with the principal's id (2), not the id 1 supplied in the body.
        verify(passwordChangeService).changePassword(2, "password123", "brand-new-secret");
    }

    // A wrong current password must not come back as 401: the session is valid, and the
    // frontend treats 401 as "session gone" and redirects away from the form.
    @Test
    void changePassword_shouldReturnBadRequestForAWrongCurrentPassword() throws Exception {
        doThrow(new IllegalArgumentException("Current password is incorrect."))
                .when(passwordChangeService).changePassword(anyInt(), anyString(), anyString());

        mockMvc.perform(patch("/api/auth/password")
                        .with(asUser(2, UserRole.USER))
                        .with(csrf())
                        .with(fromAddress("192.0.2.12"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect."));
    }

    @Test
    void changePassword_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(csrf())
                        .with(fromAddress("192.0.2.13"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verify(passwordChangeService, never()).changePassword(anyInt(), anyString(), anyString());
    }

    @Test
    void changePassword_shouldReturnForbiddenWithoutCsrfToken() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(asUser(2, UserRole.USER))
                        .with(fromAddress("192.0.2.14"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(passwordChangeService, never()).changePassword(anyInt(), anyString(), anyString());
    }

    // Repeated wrong current passwords are credential guesses, so they consume the same budget
    // as failed logins — otherwise this endpoint is an unthrottled oracle for the real password.
    @Test
    void changePassword_shouldThrottleAfterRepeatedFailures() throws Exception {
        doThrow(new IllegalArgumentException("Current password is incorrect."))
                .when(passwordChangeService).changePassword(anyInt(), anyString(), anyString());

        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(patch("/api/auth/password")
                            .with(asUser(9, UserRole.USER))
                            .with(csrf())
                            .with(fromAddress("192.0.2.15"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(patch("/api/auth/password")
                        .with(asUser(9, UserRole.USER))
                        .with(csrf())
                        .with(fromAddress("192.0.2.15"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isTooManyRequests());
    }
}
