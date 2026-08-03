// v0.9.0: Write contract for the coin catalog, backing the admin catalog endpoints.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.view.CoinCatalogView;

/**
 * The write half of the live catalog path, mirroring the query/command split already used by
 * the collection controllers. Deliberately separate from the older
 * {@link CoinRepositoryInterface}: that one predates the Spring Boot rewrite, has in-memory and
 * cache-decorating implementations wired only through {@link com.vincevscode.cointracker.cli.CoinCatalogCli},
 * and isn't registered as a bean — see its doc comment.
 */
public interface CoinCatalogCommandRepositoryInterface {

    CoinCatalogView createCoin(String country, String denomination, int year);

    /**
     * @return the updated coin, or {@code null} if no coin has that id.
     */
    CoinCatalogView updateCoin(int coinId, String country, String denomination, int year);

    /**
     * @return {@code true} if a row was deleted, {@code false} if no coin had that id.
     */
    boolean deleteCoin(int coinId);

    CoinCatalogView findCoinById(int coinId);

    /**
     * How many users' collection entries point at this coin. The {@code collection_entries}
     * foreign key is {@code ON DELETE CASCADE}, so deleting a referenced coin destroys those
     * entries silently — this count is what lets the service refuse to do that unasked.
     */
    long countCollectionEntriesForCoin(int coinId);
}
