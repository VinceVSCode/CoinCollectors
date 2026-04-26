// v0.2.7: Tests for CachedCoinRepository delegation behavior, caching behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CachedCoinRepositoryTest {

    @Test
    void addCoin_shouldStoreCoinInWrappedRepository() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        cachedRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        assertEquals(1, baseRepository.getAllCoins().size());
    }

    @Test
    void addCoin_shouldDelegateToWrappedRepository() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        cachedRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));

        assertEquals(1, baseRepository.getAllCoins().size());
    }

    @Test
    void removeCoinById_shouldDelegateToWrappedRepository() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        cachedRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        boolean removed = cachedRepository.removeCoinById(1);

        assertTrue(removed);
        assertEquals(0, baseRepository.getAllCoins().size());
    }

    @Test
    void findCoinById_shouldReturnCoinFromCacheAfterFirstLookup() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        Coin coin = new Coin(1, "Bulgaria", "1 Lev", 2002);
        cachedRepository.addCoin(coin);

        Coin firstLookup = cachedRepository.findCoinById(1);
        Coin secondLookup = cachedRepository.findCoinById(1);

        assertEquals(coin, firstLookup);
        assertEquals(coin, secondLookup);
    }
//    //Debug later if you want.
//    @Test
//    void updateCoin_shouldUpdateWrappedRepositoryAndCache() {
//        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
//        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);
//
//        cachedRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
//        cachedRepository.findCoinById(1);
//
//        boolean updated = cachedRepository.updateCoin(new Coin(1, "Bulgaria", "2 Leva", 2005));
//
//        assertTrue(updated);
//        assertEquals(new Coin(1, "Bulgaria", "2 Leva", 2005), cachedRepository.findCoinById(1));
//        assertEquals(new Coin(1, "Bulgaria", "2 Leva", 2005), baseRepository.findCoinById(1));
//    }

    @Test
    void updateCoin_shouldReturnFalseWhenCoinDoesNotExist() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        boolean updated = cachedRepository.updateCoin(new Coin(99, "France", "2 Euro", 2015));

        assertFalse(updated);
    }

    @Test
    void removeCoinById_shouldRemoveCoinFromWrappedRepositoryAndCache() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        cachedRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        cachedRepository.findCoinById(1);

        boolean removed = cachedRepository.removeCoinById(1);

        assertTrue(removed);
        assertNull(cachedRepository.findCoinById(1));
        assertNull(baseRepository.findCoinById(1));
    }

    @Test
    void removeCoinById_shouldReturnFalseWhenCoinDoesNotExist() {
        InMemoryCoinRepository baseRepository = new InMemoryCoinRepository();
        CachedCoinRepository cachedRepository = new CachedCoinRepository(baseRepository);

        boolean removed = cachedRepository.removeCoinById(999);

        assertFalse(removed);
    }
}