// v0.2.8: Repository wrapper with TTL-based in-memory caching for coin lookups by ID.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedCoinRepository implements CoinRepositoryInterface {
    private final CoinRepositoryInterface delegate;
    private final Map<Integer, CachedCoinEntry> coinByIdCache;
    private final long ttlMillis;

    public CachedCoinRepository(CoinRepositoryInterface delegate) {
        this(delegate, 60_000);
    }

    public CachedCoinRepository(CoinRepositoryInterface delegate, long ttlMillis) {
        this.delegate = delegate;
        this.coinByIdCache = new HashMap<>();
        this.ttlMillis = ttlMillis;
    }

    @Override
    public void addCoin(Coin coin) {
        delegate.addCoin(coin);
        coinByIdCache.put(coin.getId(), createCacheEntry(coin));
    }

    @Override
    public List<Coin> getAllCoins() {
        return delegate.getAllCoins();
    }

    @Override
    public Coin findCoinById(int id) {
        CachedCoinEntry cachedEntry = coinByIdCache.get(id);

        if (cachedEntry != null) {
            if (!isExpired(cachedEntry)) {
                return cachedEntry.getCoin();
            }

            coinByIdCache.remove(id);
        }

        Coin coin = delegate.findCoinById(id);

        if (coin != null) {
            coinByIdCache.put(id, createCacheEntry(coin));
        }

        return coin;
    }

    @Override
    public boolean updateCoin(Coin updatedCoin) {
        boolean updated = delegate.updateCoin(updatedCoin);

        if (updated) {
            coinByIdCache.put(updatedCoin.getId(), createCacheEntry(updatedCoin));
        }

        return updated;
    }

    @Override
    public boolean removeCoinById(int id) {
        boolean removed = delegate.removeCoinById(id);

        if (removed) {
            coinByIdCache.remove(id);
        }

        return removed;
    }

    private CachedCoinEntry createCacheEntry(Coin coin) {
        return new CachedCoinEntry(coin, System.currentTimeMillis());
    }

    private boolean isExpired(CachedCoinEntry cachedEntry) {
        long currentTimeMillis = System.currentTimeMillis();
        long ageMillis = currentTimeMillis - cachedEntry.getCachedAtMillis();

        return ageMillis > ttlMillis;
    }
}