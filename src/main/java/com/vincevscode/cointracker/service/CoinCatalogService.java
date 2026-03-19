// v0.1.6 Service layer for coin catalog operations.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.repository.CoinRepositoryInterface;


import java.util.List;

public class CoinCatalogService {

    private CoinRepositoryInterface coinRepository;

    public CoinCatalogService(CoinRepositoryInterface coinRepository) {
        this.coinRepository = coinRepository;
    }

    public void addCoin(Coin coin) {
        // First validate then entry.
        validateCoinData(
                coin.getId(),
                coin.getCountry(),
                coin.getDenomination(),
                coin.getYear()
        );

        coinRepository.addCoin(coin);
    }

    public void addCoin(int id, String country, String denomination, int year) {
        // First validate then handle any entry.
        validateCoinData(id, country, denomination, year);

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

    private void validateCoinData(int id, String country, String denomination, int year) {
        if (id <= 0) {
            throw new IllegalArgumentException("Coin ID must be greater than 0.");
        }

        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be blank.");
        }

        if (denomination == null || denomination.isBlank()) {
            throw new IllegalArgumentException("Denomination cannot be blank.");
        }

        if (year <= 0) {
            throw new IllegalArgumentException("Year must be greater than 0.");
        }
    }
}
