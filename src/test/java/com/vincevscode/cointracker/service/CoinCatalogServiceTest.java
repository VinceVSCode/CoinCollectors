// created at v0.1.6 for simple services Testing
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.CoinRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoinCatalogServiceTest {

    @Test
    void addCoin_shouldStoreCoinThroughService() {
        CoinRepository repository = new CoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        service.addCoin(1, "Bulgaria", "1 Lev", 2002);

        assertEquals(1, service.getAllCoins().size());
        assertEquals("Bulgaria", service.getAllCoins().get(0).getCountry());
    }

    @Test
    void removeCoinById_shouldRemoveCoinThroughService() {
        CoinRepository repository = new CoinRepository();
        CoinCatalogService service = new CoinCatalogService(repository);

        service.addCoin(1, "Bulgaria", "1 Lev", 2002);
        service.addCoin(2, "Germany", "1 Euro", 2010);

        boolean removed = service.removeCoinById(2);

        assertTrue(removed);
        assertEquals(1, service.getAllCoins().size());
    }
}
