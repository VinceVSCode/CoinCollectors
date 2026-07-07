// v0.7.3: Unit tests for AccountStatusFilter (immediate effect of account deactivation).
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountStatusFilterTest {
    private AuthUserQueryService authUserQueryService;
    private AccountStatusFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        authUserQueryService = mock(AuthUserQueryService.class);
        filter = new AccountStatusFilter(authUserQueryService);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(int userId, boolean active) {
        AuthUser authUser = new AuthUser(userId, "user" + userId, "hash", UserRole.USER, active, LocalDateTime.now());
        AuthUserDetails principal = new AuthUserDetails(authUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private MockHttpServletRequest apiRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/coins");
        request.setServletPath("/api/coins");
        request.getSession(); // establish a session so we can assert it is invalidated
        return request;
    }

    @Test
    void shouldRejectDeactivatedUserWithoutCallingTheChain() throws Exception {
        authenticateAs(2, true);
        when(authUserQueryService.findAuthUserById(2))
                .thenReturn(new AuthUser(2, "user2", "hash", UserRole.USER, false, LocalDateTime.now()));

        MockHttpServletRequest request = apiRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("deactivated"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowActiveUser() throws Exception {
        authenticateAs(2, true);
        when(authUserQueryService.findAuthUserById(2))
                .thenReturn(new AuthUser(2, "user2", "hash", UserRole.USER, true, LocalDateTime.now()));

        MockHttpServletRequest request = apiRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldAllowWhenLookupReturnsNull() throws Exception {
        // Test-safety / deleted-row case: a missing lookup must not block (only a positive inactive finding does).
        authenticateAs(2, true);
        when(authUserQueryService.findAuthUserById(2)).thenReturn(null);

        MockHttpServletRequest request = apiRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldNotFilterUnauthenticatedRequests() throws Exception {
        MockHttpServletRequest request = apiRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldSkipNonApiPathsEvenForDeactivatedUser() throws Exception {
        authenticateAs(2, true);
        when(authUserQueryService.findAuthUserById(2))
                .thenReturn(new AuthUser(2, "user2", "hash", UserRole.USER, false, LocalDateTime.now()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login.html");
        request.setServletPath("/login.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        // Static pages/auth entry points are skipped so a deactivated user can still reach login/logout.
        verify(chain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldSkipLogoutSoDeactivatedUserCanEndSession() throws Exception {
        authenticateAs(2, true);
        when(authUserQueryService.findAuthUserById(2))
                .thenReturn(new AuthUser(2, "user2", "hash", UserRole.USER, false, LocalDateTime.now()));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/logout");
        request.setServletPath("/api/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
