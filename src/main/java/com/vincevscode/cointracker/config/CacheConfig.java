// v0.2.8: Cache configuration loaded from environment variables.
package com.vincevscode.cointracker.config;

/**
 * Reads {@code COIN_TRACKER_CACHE_MODE} to pick a {@link CacheMode} (and thus a TTL) for
 * {@link com.vincevscode.cointracker.repository.CachedCoinRepository} when constructed via
 * {@link RepositoryFactory}. Part of the pre-Spring-Boot repository path — see
 * {@link com.vincevscode.cointracker.repository.CoinRepositoryInterface}'s doc for why that
 * path isn't reachable from the live app today.
 */
public class CacheConfig {
    private final CacheMode cacheMode;

    public CacheConfig(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public static CacheConfig fromEnvironment(){
        String cacheModeValue = System.getenv("COIN_TRACKER_CACHE_MODE");
        CacheMode cacheMode = CacheMode.fromEnviromentValue(cacheModeValue);

        return new CacheConfig(cacheMode);
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public long getTtlMillis() {
        return cacheMode.getTtlMillis();
    }
}
