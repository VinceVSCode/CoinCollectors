// v0.5.1: Spring Security plumbing (password hashing, UserDetailsService); no route enforcement yet.
// v0.6.0: Require authentication for all routes except register/login/static assets; CSRF via cookie for the JS frontend.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.security.AccountStatusFilter;
import com.vincevscode.cointracker.security.AuthUserDetailsService;
import com.vincevscode.cointracker.security.CsrfCookieFilter;
import com.vincevscode.cointracker.security.JsonAccessDeniedHandler;
import com.vincevscode.cointracker.security.JsonAuthenticationEntryPoint;
import com.vincevscode.cointracker.security.SpaCsrfTokenRequestHandler;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * The whole app's authn/authz posture in one place. {@code @EnableMethodSecurity} turns on
 * {@code @PreAuthorize} for the owner-or-admin checks scattered across the api/ controllers;
 * everything else here builds the session-cookie login flow and makes sure every error path
 * returns JSON instead of Spring Security's default HTML (see the security/ package).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: salts automatically, deliberately slow (tunable work factor) to resist
        // brute-force/rainbow-table attacks on a leaked password_hash column.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AuthUserQueryService authUserQueryService) {
        return new AuthUserDetailsService(authUserQueryService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Persist the authenticated principal in the HttpSession (server-side session-cookie
        // auth) rather than the stateless default — required so identity survives across
        // requests for a plain-JS frontend with no JWT/bearer-token handling.
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            AuthUserQueryService authUserQueryService
    ) throws Exception {
        http
                .securityContext(securityContext -> securityContext.securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf
                        // withHttpOnlyFalse: the XSRF-TOKEN cookie must be JS-readable so
                        // js/auth.js can copy its value into the X-XSRF-TOKEN header on
                        // state-changing requests — this is the standard double-submit-cookie
                        // CSRF pattern for a plain-JS frontend with no server-rendered forms.
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Only the entry points a not-yet-authenticated user must be able to
                        // reach are open; everything else (including every /api/** business
                        // route) requires a session, enforced by anyRequest().authenticated().
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/admin.html").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler())
                )
                // Filter order matters: CsrfCookieFilter must run (forcing token generation)
                // before AccountStatusFilter can meaningfully act on an authenticated principal;
                // both run after Spring Security's own BasicAuthenticationFilter in the chain.
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new AccountStatusFilter(authUserQueryService), CsrfCookieFilter.class);

        return http.build();
    }
}
