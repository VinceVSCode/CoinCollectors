// v0.4.0: Repository contract for collection entry storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;

import java.util.List;

/**
 * Storage contract for per-user coin ownership (see {@link CollectionEntry}). The
 * getOwned/getMissing methods are overloaded from "no filter" to "filter" to "full paged
 * query" so callers only pay for the query-building complexity they actually need
 * (see {@link com.vincevscode.cointracker.query.OwnedCoinQuery} / {@link com.vincevscode.cointracker.query.MissingCoinQuery}).
 * "Missing" coins are computed as catalog coins the user has no entry for (or quantity 0),
 * not stored directly — so it's effectively an anti-join against the catalog.
 */
public interface CollectionEntryRepositoryInterface {
    CollectionEntry addCollectionEntry(int userId, int coinId, int quantity);

    List<CollectionEntry> getAllCollectionEntries();

    CollectionEntry findCollectionEntryById(int id);

    CollectionEntry findCollectionEntryByUserIdAndCoinId(int userId, int coinId);

    boolean updateCollectionEntry(CollectionEntry updatedCollectionEntry);

    List<OwnedCoinView> getOwnedCoinsForUser(int userId);

    List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinFilter filter);

    List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinQuery query);

    List<MissingCoinView> getMissingCoinsForUser(int userId);

    List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinFilter filter);

    List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinQuery query);

    // Used by CollectionTrackingService.getCollectionProgress to compute percentage-complete
    // without pulling full row lists into memory.
    long countOwnedCoinsForUser(int userId, OwnedCoinFilter filter);

    long countMissingCoinsForUser(int userId, MissingCoinFilter filter);

}