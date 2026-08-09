// v0.6.0: Controller tests for registration, login, logout, and current-session identity.
// Runs with the real SecurityConfig filter chain (unlike the other controller tests) since login/session/CSRF
// mechanics are exactly what this class needs to exercise.
package com.vincevscode.cointracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincevscode.cointracker.api.dto.LoginRequest;
import com.vincevscode.cointracker.api.dto.RegisterRequest;
import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.security.AuthUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.vincevscode.cointracker.support.AuthTestSupport.fromAddress;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRegistrationService userRegistrationService;

    // Unused directly; only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void register_shouldReturnCreatedUserAsJson() throws Exception {
        when(userRegistrationService.register("newuser", "password123"))
                .thenReturn(new AuthUser(4, "newuser", "hash", UserRole.USER, true, LocalDateTime.now()));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("newuser", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_shouldReturnAuthenticatedUserAsJson() throws Exception {
        AuthUser authUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        AuthUserDetails principal = new AuthUserDetails(authUser);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .with(fromAddress("198.51.100.10"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("vince", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("vince"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .with(fromAddress("198.51.100.11"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("vince", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password."));
    }

    @Test
    void me_shouldReturnCurrentAuthenticatedUser() throws Exception {
        AuthUser authUser = new AuthUser(2, "alex", "hash", UserRole.USER, true, LocalDateTime.now());
        AuthUserDetails principal = new AuthUserDetails(authUser);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        mockMvc.perform(get("/api/auth/me").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.username").value("alex"));
    }

    @Test
    void me_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturnOkForAuthenticatedUser() throws Exception {
        AuthUser authUser = new AuthUser(2, "alex", "hash", UserRole.USER, true, LocalDateTime.now());
        AuthUserDetails principal = new AuthUserDetails(authUser);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .with(authentication(authentication)))
                .andExpect(status().isOk());
    }
}
