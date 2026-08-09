// v0.4.0: REST exception handler for request validation errors.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.security.TooManyLoginAttemptsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global @ExceptionHandler advice that turns internal exceptions into the same
 * {@code {"error": "..."}} JSON shape used by {@link com.vincevscode.cointracker.security.JsonAuthenticationEntryPoint}
 * / {@link com.vincevscode.cointracker.security.JsonAccessDeniedHandler} for the 401/403 cases
 * those handle instead. This is the layer that turns a service's
 * {@code IllegalArgumentException("Coin was not found.")} into a clean 400 rather than a raw
 * 500 leaking a stack trace — the fix from the security/pen-test hardening pass.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    // Every service-layer validation failure throws IllegalArgumentException with a
    // user-safe message — that convention is what makes this one handler sufficient for all
    // of them, rather than needing a distinct exception type per validation rule.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    // Malformed/missing JSON body (e.g. wrong content-type, truncated body) — deliberately
    // returns a generic message rather than exception.getMessage(), which can contain raw
    // Jackson parser internals not meant for API clients.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return Map.of("error", "Request body is missing or invalid.");
    }

    // Thrown by AuthenticationManager.authenticate() in AuthController#login on bad
    // credentials. Message is intentionally generic (not "user not found" vs "wrong password")
    // to avoid username enumeration — confirmed as a safe behavior in the security pen-test pass.
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException exception) {
        return Map.of("error", "Invalid username or password.");
    }

    // Thrown by LoginRateLimiter before credentials are checked. Kept distinct from the 401
    // above on purpose: answering "invalid username or password" would be untrue (nothing was
    // verified) and would leave a legitimate user unable to tell a wrong password from a
    // temporary lockout. The message carries no signal about whether the account exists.
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyLoginAttemptsException(
            TooManyLoginAttemptsException exception
    ) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(Map.of("error", exception.getMessage()));
    }
}