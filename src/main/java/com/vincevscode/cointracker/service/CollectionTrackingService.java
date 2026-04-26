// v0.3.0: Service layer for tracking user coin quantities.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;

public class CollectionTrackingService {
    private final CollectionEntryRepositoryInterface collectionEntryRepository;

    public CollectionTrackingService(CollectionEntryRepositoryInterface collectionEntryRepository) {
        this.collectionEntryRepository = collectionEntryRepository;
    }

    public void setCoinQuantity(int entryId, int userId, int coinId, int quantity) {
        validateIdsAndQuantity(entryId, userId, coinId, quantity);

        CollectionEntry existingEntry =
                collectionEntryRepository.findCollectionEntryByUserIdAndCoinId(userId, coinId);

        if (existingEntry != null) {
            CollectionEntry updatedEntry = new CollectionEntry(
                    existingEntry.getId(),
                    userId,
                    coinId,
                    quantity
            );

            collectionEntryRepository.updateCollectionEntry(updatedEntry);
            return;
        }

        CollectionEntry newEntry = new CollectionEntry(entryId, userId, coinId, quantity);
        collectionEntryRepository.addCollectionEntry(newEntry);
    }

    public CollectionEntry findCollectionEntryByUserIdAndCoinId(int userId, int coinId) {
        return collectionEntryRepository.findCollectionEntryByUserIdAndCoinId(userId, coinId);
    }

    private void validateIdsAndQuantity(int entryId, int userId, int coinId, int quantity) {
        if (entryId <= 0) {
            throw new IllegalArgumentException("Collection entry ID must be greater than 0.");
        }

        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        if (coinId <= 0) {
            throw new IllegalArgumentException("Coin ID must be greater than 0.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
    }
}