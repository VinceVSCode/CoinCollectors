// v0.1.6 Service layer for coin catalog operations.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.repository.CoinRepository;

import java.util.List;

public class CoinCatalogService {

    private CoinRepository coinRepository;

    public CoinCatalogService(CoinRepository coinRepository) {
        this.coinRepository = coinRepository;
    }

    public void addCoin(Coin coin) {
        coinRepository.addCoin(coin);
    }

    public void addCoin(int id, String country, String denomination, int year) {
        Coin coin = new Coin(id, country, denomination, year);
        coinRepository.addCoin(coin);
    }

    public List<Coin> getAllCoins() {
        return coinRepository.getAllCoins();
    }

    public Coin findCoinById(int id) {
        return coinRepository.findCoinById(id);
    }

    public boolean removeCoinById(int id) {
        return coinRepository.removeCoinById(id);
    }
}
