// v0.2.28 Supported cache modes with predefined TTL values.
package com.vincevscode.cointracker.config;

/**
 * Named TTL presets (in milliseconds) for {@link CachedCoinRepository}, selected via
 * {@code COIN_TRACKER_CACHE_MODE}. Defaults to MEDIUM when unset — see {@link #fromEnviromentValue}.
 */
public enum CacheMode {
    SHORT(15_000),
    MEDIUM(60_000),
    LONG(600_000);

    private final long ttlMillis;
    CacheMode (long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }

    public static CacheMode fromEnviromentValue(String value){
        // No env var set: default to MEDIUM rather than throwing, so caching "just works"
        // without requiring every deployment to opt in explicitly.
        if (value == null || value.isBlank()){
            return MEDIUM;
        }

        if (value.equalsIgnoreCase("short")) {
            return SHORT;
        }

        if (value.equalsIgnoreCase("medium")) {
            return MEDIUM;
        }

        if (value.equalsIgnoreCase("long")) {
            return LONG;
        }

        // In case of junk
        throw new IllegalStateException(
                "Unsupported cache mode: " + value +
                ". Use 'short', 'medium', or 'long'."
        );
    }
}
