// v0.1.7: Created at.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.List;

/**
 * Basic CRUD contract for the coin catalog, implemented by {@link InMemoryCoinRepository},
 * {@link PostgresCoinRepository}, and the cache-decorating {@link CachedCoinRepository}.
 * Predates the Spring Boot / auth rewrite: {@link com.vincevscode.cointracker.config.RepositoryFactory}
 * can select between these by env var, but nothing in the current Spring wiring
 * ({@link com.vincevscode.cointracker.config.ApplicationConfiguration}) actually calls it —
 * the live app's catalog writes go through {@link com.vincevscode.cointracker.repository.PostgresCoinCatalogQueryRepository}
 * (reads) and this interface's Postgres impl only via {@link com.vincevscode.cointracker.cli.CoinCatalogCli},
 * which is itself not registered as a Spring bean. Left in place as pre-REST-API groundwork.
 */
public interface CoinRepositoryInterface {
    void addCoin(Coin coin);

    List<Coin> getAllCoins();

    Coin findCoinById(int id);

    boolean updateCoin(Coin updatedCoin);

    boolean removeCoinById(int id);
}
