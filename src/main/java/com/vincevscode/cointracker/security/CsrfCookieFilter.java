// v0.6.0: Forces the CSRF token to be loaded (and its cookie written) on every request.
package com.vincevscode.cointracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security's CSRF support is lazy: it only actually generates the token (and, with
 * {@link org.springframework.security.web.csrf.CookieCsrfTokenRepository}, writes the
 * {@code XSRF-TOKEN} cookie) the first time something calls {@code csrfToken.getToken()}.
 * Without this filter, a plain page load would never trigger that call, and the frontend's
 * {@code js/auth.js} would have no cookie to read the token from for its first POST.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            // The return value is unused — calling getToken() is what forces token
            // generation/cookie-write as a side effect; that's the entire point of this filter.
            csrfToken.getToken();
        }

        filterChain.doFilter(request, response);
    }
}
