// v0.3.0: Repository contract for collection entry storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.CollectionEntry;
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

    List<MissingCoinView> getMissingCoinsForUser(int userId);
}