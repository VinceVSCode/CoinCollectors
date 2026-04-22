// v0.2.8: Cache configuration loaded from environment variables.
package com.vincevscode.cointracker.config;

public class CacheConfig {
    private final CacheMode cacheMode;

    // constructor
    public CacheConfig(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public static CacheConfig fromEnvironment(){

        // Check the env for which mode
        String cacheModeValue = System.getenv("COIN_TRACKER_CACHE_MODE");
        //appoint the value from our CacheMode enum class.
        CacheMode cacheMode = CacheMode.fromEnviromentValue(cacheModeValue);

        // return
        return new CacheConfig(cacheMode);

    }

    // return the final private var cacheMode. Maybe for future checks and modifications
    public CacheMode getCacheMode() {
        return cacheMode;
    }

    // return current ttl
    public long getTtlMillis() {
        return cacheMode.getTtlMillis();
    }
}
