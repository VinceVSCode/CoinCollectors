// v0.2.7: Repository wrapper prepared for future caching behavior.
// v0.2.7: Repository wrapper with basic in-memory caching for coin lookups by ID.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedCoinRepository implements CoinRepositoryInterface {
    private final CoinRepositoryInterface delegate;
    private final Map<Integer, Coin> coinByIdCache;

    public CachedCoinRepository(CoinRepositoryInterface delegate) {
        this.delegate = delegate;
        this.coinByIdCache = new HashMap<>();
    }

    @Override
    public void addCoin(Coin coin) {
        delegate.addCoin(coin);
        coinByIdCache.put(coin.getId(), coin);
    }

    @Override
    public List<Coin> getAllCoins() {
        return delegate.getAllCoins();
    }

    @Override
    public Coin findCoinById(int id) {
        if (coinByIdCache.containsKey(id)) {
            return coinByIdCache.get(id);
        }

        Coin coin = delegate.findCoinById(id);

        if (coin != null) {
            coinByIdCache.put(id, coin);
        }

        return coin;
    }

    @Override
    public boolean updateCoin(Coin updatedCoin) {
        boolean updated = delegate.updateCoin(updatedCoin);

        if (updated) {
            coinByIdCache.put(updatedCoin.getId(), updatedCoin);
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
}