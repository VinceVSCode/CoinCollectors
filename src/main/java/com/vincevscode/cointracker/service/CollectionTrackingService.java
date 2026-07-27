// v0.4.0: Service layer for tracking user coin quantities with transactional write operations.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.query.PageRequest;
import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.view.CollectionProgressView;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core business logic for per-user coin ownership: setting quantities, listing owned/missing
 * coins, and computing collection progress. Callers (controllers) are expected to have already
 * authorized that the caller may act on {@code userId} via {@code @PreAuthorize} — this class
 * only validates data shape, not who's allowed to call it.
 */
public class CollectionTrackingService {
    private final CollectionEntryRepositoryInterface collectionEntryRepository;

    public CollectionTrackingService(CollectionEntryRepositoryInterface collectionEntryRepository) {
        this.collectionEntryRepository = collectionEntryRepository;
    }

    @Transactional
    public CollectionEntry setCoinQuantity(int userId, int coinId, int quantity) {
        validateUserId(userId);
        validateCoinId(coinId);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        CollectionEntry existingEntry =
                collectionEntryRepository.findCollectionEntryByUserIdAndCoinId(userId, coinId);

        if (existingEntry != null) {
            // Setting quantity to 0 does NOT delete the row — it's kept and treated as "not
            // owned" (see the owned/missing SQL in PostgresCollectionEntryRepository), which
            // keeps this an idempotent upsert rather than needing separate add/remove paths.
            CollectionEntry updatedEntry = new CollectionEntry(
                    existingEntry.getId(),
                    userId,
                    coinId,
                    quantity
            );

            collectionEntryRepository.updateCollectionEntry(updatedEntry);
            return updatedEntry;
        }

        try {
            return collectionEntryRepository.addCollectionEntry(userId, coinId, quantity);
        } catch (DataIntegrityViolationException exception) {
            // The coin_id foreign key is the only user-supplied reference that can be invalid here
            // (userId comes from the authenticated principal), so surface it as a clean domain error.
            throw new IllegalArgumentException("Coin was not found.");
        }
    }

    @Transactional(readOnly = true)
    public CollectionEntry findCollectionEntryByUserIdAndCoinId(int userId, int coinId) {
        validateUserId(userId);
        validateCoinId(coinId);

        return collectionEntryRepository.findCollectionEntryByUserIdAndCoinId(userId, coinId);
    }

    @Transactional(readOnly = true)
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId) {
        validateUserId(userId);
        return collectionEntryRepository.getOwnedCoinsForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinFilter filter) {
        validateUserId(userId);
        validateOwnedCoinFilter(filter);
        return collectionEntryRepository.getOwnedCoinsForUser(userId, filter);
    }

    @Transactional(readOnly = true)
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinQuery query) {
        validateUserId(userId);
        validateOwnedCoinQuery(query);
        return collectionEntryRepository.getOwnedCoinsForUser(userId, query);
    }

    @Transactional(readOnly = true)
    public List<MissingCoinView> getMissingCoinsForUser(int userId) {
        validateUserId(userId);
        return collectionEntryRepository.getMissingCoinsForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinFilter filter) {
        validateUserId(userId);
        validateMissingCoinFilter(filter);
        return collectionEntryRepository.getMissingCoinsForUser(userId, filter);
    }

    @Transactional(readOnly = true)
    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinQuery query) {
        validateUserId(userId);
        validateMissingCoinQuery(query);
        return collectionEntryRepository.getMissingCoinsForUser(userId, query);
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

    public long countOwnedCoinsForUser(int userId, OwnedCoinFilter filter) {
        validateUserId(userId);
        validateOwnedCoinFilter(filter);

        return collectionEntryRepository.countOwnedCoinsForUser(userId, filter);
    }

    public long countMissingCoinsForUser(int userId, MissingCoinFilter filter) {
        validateUserId(userId);
        validateMissingCoinFilter(filter);

        return collectionEntryRepository.countMissingCoinsForUser(userId, filter);
    }

    @Transactional(readOnly = true)
    public CollectionProgressView getCollectionProgress(int userId) {
        validateUserId(userId);

        long ownedCoinCount = collectionEntryRepository.countOwnedCoinsForUser(userId, null);
        long missingCoinCount = collectionEntryRepository.countMissingCoinsForUser(userId, null);
        long totalCoinsInCatalog = ownedCoinCount + missingCoinCount;

        // Guard divide-by-zero for an empty catalog; round to 1 decimal by scaling to
        // tenths-of-a-percent before rounding, then scaling back down (e.g. 16.666...% -> 16.7%).
        double percentageComplete = totalCoinsInCatalog == 0
                ? 0.0
                : Math.round((ownedCoinCount * 1000.0) / totalCoinsInCatalog) / 10.0;

        return new CollectionProgressView(
                userId,
                totalCoinsInCatalog,
                ownedCoinCount,
                missingCoinCount,
                percentageComplete
        );
    }

}