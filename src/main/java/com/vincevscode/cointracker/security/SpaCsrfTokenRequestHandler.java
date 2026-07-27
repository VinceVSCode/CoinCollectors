// v0.6.0: CSRF token request handler for a cookie-reading JS frontend (no server-rendered forms).
package com.vincevscode.cointracker.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Spring Security's default CSRF handler (XorCsrfTokenRequestAttributeHandler) XOR-encodes
 * the token it hands to server-rendered form fields, which a plain-JS SPA reading the raw
 * {@code XSRF-TOKEN} cookie value can't reproduce. This handler keeps the XOR delegate for
 * writing the token (defense against BREACH-style attacks) but, when reading a token back from
 * a request, prefers the raw value sent in the {@code X-XSRF-TOKEN} header — exactly what
 * {@code js/auth.js}'s fetchJson sends — falling back to the XOR-aware path otherwise.
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());

        return StringUtils.hasText(headerValue)
                ? super.resolveCsrfTokenValue(request, csrfToken)
                : delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}
