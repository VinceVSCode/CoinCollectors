// v0.3.0: Service layer for tracking user coin quantities.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.query.PageRequest;
import java.util.List;

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
        validateUserId(userId);
        validateCoinId(coinId);

        return collectionEntryRepository.findCollectionEntryByUserIdAndCoinId(userId, coinId);
    }

    public List<OwnedCoinView> getOwnedCoinsForUser(int userId) {
        validateUserId(userId);

        return collectionEntryRepository.getOwnedCoinsForUser(userId);
    }

    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinFilter filter) {
        validateUserId(userId);
        validateOwnedCoinFilter(filter);

        return collectionEntryRepository.getOwnedCoinsForUser(userId, filter);
    }

    public List<MissingCoinView> getMissingCoinsForUser(int userId) {
        validateUserId(userId);

        return collectionEntryRepository.getMissingCoinsForUser(userId);
    }

    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinFilter filter) {
        validateUserId(userId);
        validateMissingCoinFilter(filter);

        return collectionEntryRepository.getMissingCoinsForUser(userId, filter);
    }

    private void validateMissingCoinFilter(MissingCoinFilter filter) {
        if (filter == null) {
            return;
        }

        if (filter.getMinYear() != null && filter.getMinYear() <= 0) {
            throw new IllegalArgumentException("Minimum year must be greater than 0.");
        }

        if (filter.getMaxYear() != null && filter.getMaxYear() <= 0) {
            throw new IllegalArgumentException("Maximum year must be greater than 0.");
        }

        if (filter.getMinYear() != null
                && filter.getMaxYear() != null
                && filter.getMinYear() > filter.getMaxYear()) {
            throw new IllegalArgumentException("Minimum year cannot be greater than maximum year.");
        }
    }

    private void validateIdsAndQuantity(int entryId, int userId, int coinId, int quantity) {
        if (entryId <= 0) {
            throw new IllegalArgumentException("Collection entry ID must be greater than 0.");
        }

        validateUserId(userId);
        validateCoinId(coinId);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
    }

    private void validateCoinId(int coinId) {
        if (coinId <= 0) {
            throw new IllegalArgumentException("Coin ID must be greater than 0.");
        }
    }

    private void validateOwnedCoinFilter(OwnedCoinFilter filter) {
        if (filter == null) {
            return;
        }

        if (filter.getMinYear() != null && filter.getMinYear() <= 0) {
            throw new IllegalArgumentException("Minimum year must be greater than 0.");
        }

        if (filter.getMaxYear() != null && filter.getMaxYear() <= 0) {
            throw new IllegalArgumentException("Maximum year must be greater than 0.");
        }

        if (filter.getMinYear() != null
                && filter.getMaxYear() != null
                && filter.getMinYear() > filter.getMaxYear()) {
            throw new IllegalArgumentException("Minimum year cannot be greater than maximum year.");
        }

        if (filter.getMinQuantity() != null && filter.getMinQuantity() < 0) {
            throw new IllegalArgumentException("Minimum quantity cannot be negative.");
        }
    }

    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinQuery query) {
        validateUserId(userId);
        validateOwnedCoinQuery(query);

        return collectionEntryRepository.getOwnedCoinsForUser(userId, query);
    }

    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinQuery query) {
        validateUserId(userId);
        validateMissingCoinQuery(query);

        return collectionEntryRepository.getMissingCoinsForUser(userId, query);
    }

    private void validateOwnedCoinQuery(OwnedCoinQuery query) {
        if (query == null) {
            return;
        }

        validateOwnedCoinFilter(query.getFilter());
        validatePageRequest(query.getPageRequest());
    }

    private void validateMissingCoinQuery(MissingCoinQuery query) {
        if (query == null) {
            return;
        }

        validateMissingCoinFilter(query.getFilter());
        validatePageRequest(query.getPageRequest());
    }

    private void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            return;
        }

        if (pageRequest.getPageNumber() <= 0) {
            throw new IllegalArgumentException("Page number must be greater than 0.");
        }

        if (pageRequest.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }
    }

}