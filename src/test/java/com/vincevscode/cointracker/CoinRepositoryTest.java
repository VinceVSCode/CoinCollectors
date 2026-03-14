// v0.1.2: Tests for basic in-memory CoinRepository behavior.
package com.vincevscode.cointracker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoinRepositoryTest {

    @Test
    void addCoin_shouldStoreCoinInRepository(){
        CoinRepository repository = new CoinRepository();
        Coin coin = new Coin (1,"Bulgaria", "1 Lev", 2002);

        repository.addCoin(coin);

        List<Coin> coins = repository.getAllCoins();

        assertEquals(1, coins.size());
        assertEquals("Bulgaria",coins.get(0).getCountry());

    }
}