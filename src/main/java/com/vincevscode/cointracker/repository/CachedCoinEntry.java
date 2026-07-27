// 0.2.8 implemented.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

/**
 * Immutable (coin, cachedAtMillis) pair used by {@link CachedCoinRepository} to decide when
 * an entry has aged past its TTL and needs re-fetching from the delegate.
 */
public class CachedCoinEntry {
    private final Coin coin;
    private final long cachedAtMillis;

    public CachedCoinEntry(Coin coin, long cachedAtMillis) {
        this.coin = coin;
        this.cachedAtMillis = cachedAtMillis;
    }

    public Coin getCoin() {
        return coin;
    }

    public long getCachedAtMillis() {
        return cachedAtMillis;
    }
}