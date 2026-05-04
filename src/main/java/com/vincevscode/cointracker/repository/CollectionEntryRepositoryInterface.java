// v0.3.3: Repository contract for collection entry storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;

import java.util.List;

public interface CollectionEntryRepositoryInterface {
    void addCollectionEntry(CollectionEntry collectionEntry);

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
}