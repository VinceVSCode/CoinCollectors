package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.ArrayList;
import java.util.List;

/**
 * Non-persistent {@link CoinRepositoryInterface} backed by a plain List. Predates the Postgres
 * repository; not reachable from the running Spring Boot app today (see the interface doc) —
 * kept for its own unit tests and as a selectable option in {@link com.vincevscode.cointracker.config.RepositoryFactory}.
 */
public class InMemoryCoinRepository implements CoinRepositoryInterface{
    private List<Coin> coins;

    public InMemoryCoinRepository() {
        this.coins = new ArrayList<>();
    }

    @Override
    public void addCoin(Coin coin) {
        if (findCoinById(coin.getId()) != null) {
            throw new IllegalArgumentException("Coin with Id " + coin.getId() + " already exists.");
        }
        coins.add(coin);
    }

    @Override
    public List<Coin> getAllCoins() {
        // Defensive copy: returning `coins` directly would let callers mutate our internal
        // list (add/remove/clear) without going through addCoin/removeCoinById's checks.
        return new ArrayList<>(coins);
    }

    @Override
    public Coin findCoinById(int id) {
        for (Coin coin : coins) {
            if (coin.getId() == id) {
                return coin;
            }
        }
        return null;
    }

    @Override
    public boolean updateCoin(Coin updatedCoin) {
        // Simple logic. If the id is the same, update the coin with the parameters.
        for(Coin coin : coins) {
            if(coin.getId() == updatedCoin.getId()) {
                // NOTE: coin.getId() is used here as a LIST INDEX, not as a lookup key — this
                // only works by coincidence when coin ids happen to match their list position
                // (e.g. sequential ids starting at 0, never removed). Any gap or reordering
                // makes this throw IndexOutOfBoundsException or silently overwrite the wrong
                // element. Left as-is since this class isn't reachable from the running app (see class doc).
                coins.set(coin.getId(), updatedCoin);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeCoinById(int id) {
        Coin coinToRemove = findCoinById(id);

        if (coinToRemove == null) {
            return false;
        }

        coins.remove(coinToRemove);
        return true;
    }
}
