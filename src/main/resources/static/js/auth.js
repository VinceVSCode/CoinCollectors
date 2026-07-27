// v0.6.2: Shared frontend helpers for session-aware fetch, CSRF, and auth actions.
// Loaded by every page (login/register/index/admin) as a plain <script> — no build step or
// module bundler, so this is an IIFE exposing one global `CoinAuth` namespace instead of an
// ES module export.
const CoinAuth = (() => {
    // Reads a cookie by name from document.cookie's semicolon-joined string. Used specifically
    // for XSRF-TOKEN, which SecurityConfig's CookieCsrfTokenRepository.withHttpOnlyFalse()
    // deliberately makes JS-readable (see security/CsrfCookieFilter.java) for exactly this.
    function getCookie(name) {
        const match = document.cookie.match("(^|;)\\s*" + name + "\\s*=\\s*([^;]+)");
        return match ? decodeURIComponent(match.pop()) : null;
    }

    // The one function every page-specific script below routes its API calls through: attaches
    // the session cookie (credentials: same-origin), the CSRF header for any mutating request,
    // and normalizes both success and error responses to plain JS objects/exceptions so callers
    // never touch the raw Fetch API or Response object directly.
    async function fetchJson(url, options = {}) {
        const opts = { credentials: "same-origin", ...options };
        const method = (opts.method || "GET").toUpperCase();
        opts.headers = { ...(opts.headers || {}) };

        // GET/HEAD are same-origin reads with no side effects, so Spring Security's CSRF
        // filter doesn't require the token on them — only attach it where it's actually checked.
        if (method !== "GET" && method !== "HEAD") {
            const token = getCookie("XSRF-TOKEN");
            if (token) {
                opts.headers["X-XSRF-TOKEN"] = token;
            }
        }

        const response = await fetch(url, opts);

        if (!response.ok) {
            // Every backend error path (RestExceptionHandler, JsonAuthenticationEntryPoint,
            // JsonAccessDeniedHandler) returns {"error": "..."} — fall back to a generic
            // message only if the body isn't that shape (e.g. a non-JSON proxy/server error).
            let message = "Request failed.";
            try {
                const body = await response.json();
                if (body.error) {
                    message = body.error;
                }
            } catch (ignored) {
            }
            const error = new Error(message);
            error.status = response.status;
            throw error;
        }

        if (response.status === 204) {
            return null;
        }

        // Read as text first rather than calling response.json() directly, since some 2xx
        // responses (e.g. logout) have an empty body that JSON.parse would reject.
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    // Used by every protected page on load to both confirm the session is valid and fetch the
    // current user's id/username/role for rendering — a 401 here (caught by the caller) is
    // how pages detect "not logged in" and call redirectToLogin().
    function getMe() {
        return fetchJson("/api/auth/me");
    }

    async function logout() {
        // Best-effort: even if the server call fails (e.g. session already expired), still
        // navigate away — the user's intent is to leave the authenticated area either way.
        try {
            await fetchJson("/api/auth/logout", { method: "POST" });
        } catch (ignored) {
        }
        window.location.href = "login.html";
    }

    function redirectToLogin() {
        window.location.href = "login.html";
    }

    return { getCookie, fetchJson, getMe, logout, redirectToLogin };
})();
