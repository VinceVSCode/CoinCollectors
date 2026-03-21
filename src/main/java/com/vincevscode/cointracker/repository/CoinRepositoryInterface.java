// v0.1.7: Created at.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.List;

public interface CoinRepositoryInterface {
    void addCoin(Coin coin);

    List<Coin> getAllCoins();

    Coin findCoinById(int id);

    boolean updateCoin(Coin updatedCoin);

    boolean removeCoinById(int id);
}
