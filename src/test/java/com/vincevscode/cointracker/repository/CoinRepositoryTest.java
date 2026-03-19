// v0.1.2: Tests for basic in-memory CoinRepository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void findCoinById_shouldReturnCoinWhenIdExists(){
        CoinRepository repository = new CoinRepository();
        Coin coin = new Coin (2, "Germany", "1 Euro", 2010);

        repository.addCoin(coin);

        Coin foundCoin = repository.findCoinById(2);

        assertEquals(coin, foundCoin);
    }

    @Test
    void findCoinById_shouldReturnNullWhenIdDoesNotExist() {
        CoinRepository repository = new CoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        Coin foundCoin = repository.findCoinById(99);

        assertNull(foundCoin);
    }

    @Test
    void addCoin_shouldThrowExceptionWhenIdAlreadyExists() {
        CoinRepository repository = new CoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.addCoin(new Coin(1, "France", "2 Euro", 2015))
        );
        assertEquals("Coin with Id 1 already exists.", exception.getMessage());
    }

    @Test
    void removeCoinById_shouldReturnTrueWhenCoinExists() {
        CoinRepository repository = new CoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        repository.addCoin(new Coin(2, "Germany", "1 Euro", 2010));

        boolean removed = repository.removeCoinById(2);

        assertTrue(removed);
        assertEquals(1, repository.getAllCoins().size());
        assertNull(repository.findCoinById(2));
    }

    @Test
    void removeCoinById_shouldReturnFalseWhenCoinDoesNotExist() {
        CoinRepository repository = new CoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        boolean removed = repository.removeCoinById(99);

        assertFalse(removed);
        assertEquals(1, repository.getAllCoins().size());
    }

    @Test
    void getAllCoins_shouldReturnCopyOfInternalList() {
        CoinRepository repository = new CoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        List<Coin> coins = repository.getAllCoins();
        coins.clear();

        assertEquals(1, repository.getAllCoins().size());
    }
}