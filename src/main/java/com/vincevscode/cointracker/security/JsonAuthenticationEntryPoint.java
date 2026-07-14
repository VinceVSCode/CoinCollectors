// v0.6.0: Returns a JSON error body for unauthenticated requests, matching RestExceptionHandler's shape.
package com.vincevscode.cointracker.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Wired into {@code SecurityConfig} as the entry point for unauthenticated requests to
 * protected routes — returns JSON 401 instead of Spring Security's default redirect-to-login
 * page, which would make no sense for a JSON API. See {@link JsonAccessDeniedHandler} for the
 * authenticated-but-forbidden (403) counterpart.
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Authentication is required.\"}");
    }
}
