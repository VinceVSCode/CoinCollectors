// v0.2.0: Integration tests for PostgreSQL-backed coin repository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.Coin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresCoinRepositoryTest {
    private PostgresCoinRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PostgresCoinRepository();
        clearCoinsTable();
    }

    @Test
    void addCoin_shouldStoreCoinInDatabase() {
        Coin coin = new Coin(1, "Bulgaria", "1 Lev", 2002);

        repository.addCoin(coin);

        Coin foundCoin = repository.findCoinById(1);

        assertEquals(coin, foundCoin);
    }

    @Test
    void getAllCoins_shouldReturnAllStoredCoins() {
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        repository.addCoin(new Coin(2, "Germany", "1 Euro", 2010));

        List<Coin> coins = repository.getAllCoins();

        assertEquals(2, coins.size());
        assertEquals(new Coin(1, "Bulgaria", "1 Lev", 2002), coins.get(0));
        assertEquals(new Coin(2, "Germany", "1 Euro", 2010), coins.get(1));
    }

    @Test
    void findCoinById_shouldReturnCoinWhenIdExists() {
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        Coin foundCoin = repository.findCoinById(1);

        assertEquals(new Coin(1, "Bulgaria", "1 Lev", 2002), foundCoin);
    }

    @Test
    void findCoinById_shouldReturnNullWhenIdDoesNotExist() {
        Coin foundCoin = repository.findCoinById(999);

        assertNull(foundCoin);
    }

    @Test
    void updateCoin_shouldReturnTrueWhenCoinExists() {
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        boolean updated = repository.updateCoin(new Coin(1, "Bulgaria", "2 Leva", 2005));

        assertTrue(updated);
        assertEquals(new Coin(1, "Bulgaria", "2 Leva", 2005), repository.findCoinById(1));
    }

    @Test
    void updateCoin_shouldReturnFalseWhenCoinDoesNotExist() {
        boolean updated = repository.updateCoin(new Coin(99, "France", "2 Euro", 2015));

        assertFalse(updated);
    }

    @Test
    void removeCoinById_shouldReturnTrueWhenCoinExists() {
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        boolean removed = repository.removeCoinById(1);

        assertTrue(removed);
        assertNull(repository.findCoinById(1));
    }

    @Test
    void removeCoinById_shouldReturnFalseWhenCoinDoesNotExist() {
        boolean removed = repository.removeCoinById(999);

        assertFalse(removed);
    }

    private void clearCoinsTable() {
        String sql = "DELETE FROM coins";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to clear coins table before test.", exception);
        }
    }
}