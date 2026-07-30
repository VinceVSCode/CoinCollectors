// Shared UI helpers. Loaded as a plain <script> alongside auth.js — no build step or module
// bundler, so this is an IIFE exposing one global `CoinUI` namespace, matching CoinAuth's shape.
const CoinUI = (() => {
    // How long each kind of toast stays up. Errors linger noticeably longer: a success message
    // just confirms something the user already intended, but an error is new information they
    // may need to read twice or act on.
    const DISMISS_DELAY = { success: 3000, error: 7000, info: 4000 };

    let container = null;

    function ensureContainer() {
        if (!container) {
            container = document.createElement("div");
            container.className = "toast-container";
            document.body.appendChild(container);
        }
        return container;
    }

    function dismiss(toast) {
        // Guard against the auto-dismiss timer and a manual close racing each other; whichever
        // runs second would otherwise animate an element that's already been removed.
        if (!toast.isConnected) {
            return;
        }
        toast.classList.add("toast-leaving");
        toast.addEventListener("transitionend", () => toast.remove(), { once: true });
        // Fallback for when the transition never fires (reduced-motion, backgrounded tab), so a
        // toast can't get stuck on screen forever.
        setTimeout(() => toast.remove(), 400);
    }

    // kind: "success" | "error" | "info" (default). Returns the element so a caller can dismiss
    // it early if it needs to.
    function toast(message, kind = "info") {
        const element = document.createElement("div");
        element.className = `toast toast-${kind}`;

        // Errors interrupt (assertive) since they usually mean the user's action failed and the
        // page state isn't what they expect; success/info wait their turn (polite).
        element.setAttribute("role", kind === "error" ? "alert" : "status");
        element.setAttribute("aria-live", kind === "error" ? "assertive" : "polite");

        const text = document.createElement("span");
        text.className = "toast-message";
        text.textContent = message;
        element.appendChild(text);

        const close = document.createElement("button");
        close.type = "button";
        close.className = "toast-close";
        close.setAttribute("aria-label", "Dismiss notification");
        close.textContent = "×";
        close.addEventListener("click", () => dismiss(element));
        element.appendChild(close);

        ensureContainer().appendChild(element);

        // Next frame, so the browser paints the pre-transition state first and the entrance
        // animation actually runs instead of the toast just appearing in place.
        requestAnimationFrame(() => element.classList.add("toast-visible"));

        setTimeout(() => dismiss(element), DISMISS_DELAY[kind] || DISMISS_DELAY.info);
        return element;
    }

    return {
        toast,
        success: (message) => toast(message, "success"),
        error: (message) => toast(message, "error"),
        info: (message) => toast(message, "info")
    };
})();
