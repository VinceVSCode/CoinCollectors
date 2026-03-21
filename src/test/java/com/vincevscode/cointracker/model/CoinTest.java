package com.vincevscode.cointracker.model;

import com.vincevscode.cointracker.repository.InMemoryCoinRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CoinTest {

    @Test
    void updateCoin_shouldReturnFalseWhenCoinDoesNotExist() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();

        boolean updated = repository.updateCoin(new Coin(99, "France", "2 Euro", 2015));

        assertFalse(updated);
    }

    @Test
    void removeCoinById_shouldReturnTrueWhenCoinExists() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        repository.addCoin(new Coin(2, "Germany", "1 Euro", 2010));

        boolean removed = repository.removeCoinById(2);

        assertTrue(removed);
        assertEquals(1, repository.getAllCoins().size());
        assertNull(repository.findCoinById(2));
    }

    @Test
    void removeCoinById_shouldReturnFalseWhenCoinDoesNotExist() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        boolean removed = repository.removeCoinById(99);

        assertFalse(removed);
        assertEquals(1, repository.getAllCoins().size());
    }

    @Test
    void getAllCoins_shouldReturnCopyOfInternalList() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        List<Coin> coins = repository.getAllCoins();
        coins.clear();

        assertEquals(1, repository.getAllCoins().size());
    }
}
