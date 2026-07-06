// v0.6.2: Shared frontend helpers for session-aware fetch, CSRF, and auth actions.
const CoinAuth = (() => {
    function getCookie(name) {
        const match = document.cookie.match("(^|;)\\s*" + name + "\\s*=\\s*([^;]+)");
        return match ? decodeURIComponent(match.pop()) : null;
    }

    async function fetchJson(url, options = {}) {
        const opts = { credentials: "same-origin", ...options };
        const method = (opts.method || "GET").toUpperCase();
        opts.headers = { ...(opts.headers || {}) };

        if (method !== "GET" && method !== "HEAD") {
            const token = getCookie("XSRF-TOKEN");
            if (token) {
                opts.headers["X-XSRF-TOKEN"] = token;
            }
        }

        const response = await fetch(url, opts);

        if (!response.ok) {
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

        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    function getMe() {
        return fetchJson("/api/auth/me");
    }

    async function logout() {
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
