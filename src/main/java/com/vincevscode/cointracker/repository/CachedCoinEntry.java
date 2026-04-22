// 0.2.8 implemented.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

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