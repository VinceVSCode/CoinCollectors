// v0.7.3: Rejects requests from an authenticated user whose account has since been deactivated,
// so an admin deactivation takes effect on the user's next request instead of at session expiry.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AccountStatusFilter extends OncePerRequestFilter {
    private final AuthUserQueryService authUserQueryService;

    public AccountStatusFilter(AuthUserQueryService authUserQueryService) {
        this.authUserQueryService = authUserQueryService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Only guard authenticated API traffic; leave static pages and the auth entry points alone
        // (so a deactivated user can still reach the login page and log out cleanly).
        if (!path.startsWith("/api/")) {
            return true;
        }

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/logout");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof AuthUserDetails principal) {
            AuthUser current = authUserQueryService.findAuthUserById(principal.getUserId());

            // Block only on a positive "inactive" finding; a missing lookup falls through to normal handling.
            if (current != null && !current.isActive()) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Your account has been deactivated.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
