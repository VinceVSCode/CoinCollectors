// v0.7.2: Adversarial tests for AuthController (mass-assignment / privilege escalation, credential-error leakage).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.vincevscode.cointracker.support.AuthTestSupport.fromAddress;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Companion to AuthControllerTest's happy-path coverage — this class specifically probes the
// register endpoint's field allowlisting (RegisterRequest has no role/id/active fields, so
// Jackson silently drops them) and login's error-message behavior, both hardening measures
// added during the security/pen-test pass.
@WebMvcTest(AuthController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRegistrationService userRegistrationService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void register_shouldIgnoreInjectedRoleIdAndActiveFields() throws Exception {
        when(userRegistrationService.register("attacker", "password123"))
                .thenReturn(new AuthUser(7, "attacker", "hashed", UserRole.USER, true, LocalDateTime.now()));

        // Attacker tries to smuggle privileged fields into the registration body.
        mockMvc.perform(
                        post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"attacker\",\"password\":\"password123\",\"role\":\"ADMIN\",\"id\":1,\"active\":true}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        // The controller must only forward username + password; the extra fields are unbound and ignored.
        verify(userRegistrationService).register("attacker", "password123");
    }

    @Test
    void login_shouldReturnGenericMessageOnBadCredentialsToPreventEnumeration() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .with(fromAddress("203.0.113.10"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alex\",\"password\":\"wrong\"}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password."));
    }

    // The 11th attempt exceeds the configured 10-per-window budget from SecurityConfig. Asserts
    // the endpoint genuinely stops evaluating credentials, not just that the limiter counts.
    @Test
    void login_shouldReturnTooManyRequestsAfterRepeatedFailures() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(
                            post("/api/auth/login")
                                    .with(csrf())
                                    .with(fromAddress("203.0.113.11"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"username\":\"throttled-user\",\"password\":\"wrong\"}")
                    )
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .with(fromAddress("203.0.113.11"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"throttled-user\",\"password\":\"wrong\"}")
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value(containsString("Too many login attempts")));

        // Exactly 10 credential checks: the throttled request must not reach the authentication
        // manager at all, or the BCrypt work a brute force is trying to inflict still happens.
        verify(authenticationManager, times(10)).authenticate(any());
    }

    // A throttled unknown username must look identical to a throttled real one, or the 429
    // becomes the enumeration oracle the generic 401 message exists to prevent.
    @Test
    void login_shouldThrottleUnknownUsernamesIdentically() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(
                            post("/api/auth/login")
                                    .with(csrf())
                                    .with(fromAddress("203.0.113.12"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"username\":\"no-such-account\",\"password\":\"wrong\"}")
                    )
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .with(fromAddress("203.0.113.12"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"no-such-account\",\"password\":\"wrong\"}")
                )
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void login_shouldRejectMissingCsrfToken() throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"alex\",\"password\":\"password123\"}")
                )
                .andExpect(status().isForbidden());
    }
}
