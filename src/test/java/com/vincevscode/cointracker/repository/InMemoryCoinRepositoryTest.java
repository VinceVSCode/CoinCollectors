// v0.1.2: Tests for basic in-memory CoinRepository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.service.CoinCatalogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCoinRepositoryTest {

    @Test
    void addCoin_shouldStoreCoinThroughService() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        service.addCoin(1, "Bulgaria", "1 Lev", 2002);

        assertEquals(1, service.getAllCoins().size());
        assertEquals("Bulgaria", service.getAllCoins().get(0).getCountry());
    }

    @Test
    void findCoinById_shouldReturnCoinWhenIdExists() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        Coin coin = new Coin(2, "Germany", "1 Euro", 2010);

        repository.addCoin(coin);

        Coin foundCoin = repository.findCoinById(2);

        assertEquals(coin, foundCoin);
    }

    @Test
    void addCoin_shouldStoreCoinInRepository() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        Coin coin = new Coin(1, "Bulgaria", "1 Lev", 2002);

        repository.addCoin(coin);

        List<Coin> coins = repository.getAllCoins();

        assertEquals(1, coins.size());
        assertEquals("Bulgaria", coins.get(0).getCountry());
    }

    @Test
    void removeCoinById_shouldRemoveCoinThroughService() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        service.addCoin(1, "Bulgaria", "1 Lev", 2002);
        service.addCoin(2, "Germany", "1 Euro", 2010);

        boolean removed = service.removeCoinById(2);

        assertTrue(removed);
        assertEquals(1, service.getAllCoins().size());
    }

    @Test
    void findCoinById_shouldReturnNullWhenIdDoesNotExist() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        Coin foundCoin = repository.findCoinById(99);

        assertNull(foundCoin);
    }

    @Test
    void addCoin_shouldThrowExceptionWhenIdIsNotPositive() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCoin(0, "Bulgaria", "1 Lev", 2002)
        );

        assertEquals("Coin ID must be greater than 0.", exception.getMessage());
    }

    @Test
    void addCoin_shouldThrowExceptionWhenCountryIsBlank() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCoin(1, "   ", "1 Lev", 2002)
        );

        assertEquals("Country cannot be blank.", exception.getMessage());
    }

    @Test
    void addCoin_shouldThrowExceptionWhenDenominationIsBlank() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCoin(1, "Bulgaria", "", 2002)
        );

        assertEquals("Denomination cannot be blank.", exception.getMessage());
    }

    @Test
    void addCoin_shouldThrowExceptionWhenYearIsNotPositive() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCoin(1, "Bulgaria", "1 Lev", -1)
        );

        assertEquals("Year must be greater than 0.", exception.getMessage());
    }

    @Test
    void addCoin_shouldThrowExceptionWhenIdAlreadyExists() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();
        repository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.addCoin(new Coin(1, "France", "2 Euro", 2015))
        );
        assertEquals("Coin with Id 1 already exists.", exception.getMessage());
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

    @Test
    void updateCoin_shouldReturnFalseWhenCoinDoesNotExist() {
        InMemoryCoinRepository repository = new InMemoryCoinRepository();

        boolean updated = repository.updateCoin(new Coin(99, "France", "2 Euro", 2015));

        assertFalse(updated);
    }

}