// v0.6.0: REST controller for registration, login, logout, and current-session identity.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.AuthResponse;
import com.vincevscode.cointracker.api.dto.LoginRequest;
import com.vincevscode.cointracker.api.dto.RegisterRequest;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.security.AuthUserDetails;
import com.vincevscode.cointracker.security.LoginRateLimiter;
import com.vincevscode.cointracker.service.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The four entry points a not-yet-authenticated client can reach (register/login) plus the
 * two that need an existing session (logout/me). register+login are the only endpoints
 * permitAll()'d in {@link com.vincevscode.cointracker.config.SecurityConfig} — everything else
 * requires the session cookie this controller establishes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRegistrationService userRegistrationService;
    private final SecurityContextRepository securityContextRepository;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRegistrationService userRegistrationService,
            SecurityContextRepository securityContextRepository,
            LoginRateLimiter loginRateLimiter
    ) {
        this.authenticationManager = authenticationManager;
        this.userRegistrationService = userRegistrationService;
        this.securityContextRepository = securityContextRepository;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        AuthUser authUser = userRegistrationService.register(request.getUsername(), request.getPassword());

        return new AuthResponse(authUser.getId(), authUser.getUsername(), authUser.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        // getRemoteAddr(), never X-Forwarded-For: that header is client-supplied, so trusting it
        // would let an attacker mint a fresh per-address budget on every request simply by
        // varying it. If this app is ever put behind a reverse proxy, the proxy must be
        // configured as a trusted source before any forwarded-for value can be honoured here.
        String clientAddress = httpRequest.getRemoteAddr();

        // Checked before authenticate() so a throttled attempt costs no BCrypt verification —
        // which is exactly the expensive work a brute-force attempt is trying to make us do.
        loginRateLimiter.checkAllowed(request.getUsername(), clientAddress);

        // authenticate() delegates to DaoAuthenticationProvider, which calls
        // AuthUserDetailsService + the BCrypt PasswordEncoder and throws AuthenticationException
        // (-> 401 via RestExceptionHandler) on a bad username/password without distinguishing
        // which one was wrong, to avoid leaking whether a username exists.
        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationRequest);
        } catch (AuthenticationException exception) {
            loginRateLimiter.recordFailure(request.getUsername(), clientAddress);
            throw exception;
        }

        loginRateLimiter.recordSuccess(request.getUsername());

        // Manually building + saving the SecurityContext here (rather than relying on a
        // filter) is what makes this stateless-looking REST call actually establish a session:
        // saveContext persists it via HttpSessionSecurityContextRepository, and the response's
        // Set-Cookie header for the session id is what the browser sends back on every
        // subsequent request.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();

        return new AuthResponse(principal.getUserId(), principal.getUsername(), principal.getRole());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Invalidates the HttpSession and clears the SecurityContext — same effect as Spring
        // Security's default logout filter, just reachable via a JSON POST instead of a
        // server-rendered logout form/redirect.
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();

        return new AuthResponse(principal.getUserId(), principal.getUsername(), principal.getRole());
    }
}
