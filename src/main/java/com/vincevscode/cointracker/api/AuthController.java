// v0.6.0: REST controller for registration, login, logout, and current-session identity.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.AuthResponse;
import com.vincevscode.cointracker.api.dto.LoginRequest;
import com.vincevscode.cointracker.api.dto.RegisterRequest;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.security.AuthUserDetails;
import com.vincevscode.cointracker.service.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRegistrationService userRegistrationService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRegistrationService userRegistrationService,
            SecurityContextRepository securityContextRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRegistrationService = userRegistrationService;
        this.securityContextRepository = securityContextRepository;
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

        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();

        return new AuthResponse(principal.getUserId(), principal.getUsername(), principal.getRole());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();

        return new AuthResponse(principal.getUserId(), principal.getUsername(), principal.getRole());
    }
}
