// v0.9.0: Service layer for admin coin catalog management (create, update, delete).
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.repository.CoinCatalogCommandRepositoryInterface;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backs the ADMIN-only catalog endpoints ({@code AdminCoinController}). Callers are expected to
 * have already authorized the request via {@code @PreAuthorize}; this class owns data-shape
 * validation and the delete safety rule.
 *
 * <p>The load-bearing rule is the cascade guard in {@link #deleteCoin}. The
 * {@code collection_entries.coin_id} foreign key is {@code ON DELETE CASCADE} (see
 * {@code V1__initial_schema.sql}), so deleting a coin that users own doesn't fail — it silently
 * destroys their collection entries and shifts their progress. Rather than let a single misclick
 * quietly delete other people's data, deletion of a referenced coin is refused unless the caller
 * explicitly opts in, which is what turns an accident into a decision.
 */
public class CoinCatalogManagementService {
    // Matches the TEXT columns' practical use rather than a DB limit (they're unbounded TEXT);
    // stops a caller from storing something absurd in a field the UI renders in a table cell.
    private static final int MAX_TEXT_LENGTH = 100;

    // The schema's CHECK (year > 0) is the hard floor; the ceiling is a sanity bound so an
    // obvious typo (e.g. 20255) is rejected at the edge rather than stored.
    private static final int MAX_YEAR = 2999;

    private static final String TARGET_TYPE_COIN = "COIN";

    private final CoinCatalogCommandRepositoryInterface coinCatalogCommandRepository;
    private final AdminAuditRepositoryInterface adminAuditRepository;

    public CoinCatalogManagementService(
            CoinCatalogCommandRepositoryInterface coinCatalogCommandRepository,
            AdminAuditRepositoryInterface adminAuditRepository
    ) {
        this.coinCatalogCommandRepository = coinCatalogCommandRepository;
        this.adminAuditRepository = adminAuditRepository;
    }

    @Transactional
    public CoinCatalogView createCoin(AdminActor actor, String country, String denomination, Integer year) {
        requireActor(actor);

        String validatedCountry = validateText(country, "Country");
        String validatedDenomination = validateText(denomination, "Denomination");
        int validatedYear = validateYear(year);

        CoinCatalogView created = coinCatalogCommandRepository.createCoin(
                validatedCountry,
                validatedDenomination,
                validatedYear
        );

        adminAuditRepository.recordAction(
                actor,
                AdminActionType.COIN_CREATED,
                TARGET_TYPE_COIN,
                created.getCoinId(),
                describe(created)
        );

        return created;
    }

    @Transactional
    public CoinCatalogView updateCoin(AdminActor actor, int coinId, String country, String denomination, Integer year) {
        requireActor(actor);
        validateCoinId(coinId);

        String validatedCountry = validateText(country, "Country");
        String validatedDenomination = validateText(denomination, "Denomination");
        int validatedYear = validateYear(year);

        CoinCatalogView updated = coinCatalogCommandRepository.updateCoin(
                coinId,
                validatedCountry,
                validatedDenomination,
                validatedYear
        );

        if (updated == null) {
            throw new IllegalArgumentException("Coin was not found.");
        }

        adminAuditRepository.recordAction(
                actor,
                AdminActionType.COIN_UPDATED,
                TARGET_TYPE_COIN,
                coinId,
                describe(updated)
        );

        return updated;
    }

    /**
     * @param force when {@code false}, deleting a coin any user owns is refused; when
     *              {@code true}, the caller has accepted that those collection entries cascade
     *              away with it.
     */
    @Transactional
    public void deleteCoin(AdminActor actor, int coinId, boolean force) {
        requireActor(actor);
        validateCoinId(coinId);

        CoinCatalogView existing = coinCatalogCommandRepository.findCoinById(coinId);

        if (existing == null) {
            throw new IllegalArgumentException("Coin was not found.");
        }

        // Counted even when forcing, because the number of collection entries destroyed is the
        // most important thing this action leaves behind — an audit record saying only "coin
        // deleted" wouldn't capture that other users lost data.
        long affectedEntries = coinCatalogCommandRepository.countCollectionEntriesForCoin(coinId);

        if (!force && affectedEntries > 0) {
            throw new IllegalArgumentException(
                    "This coin is in " + affectedEntries + " collection "
                            + (affectedEntries == 1 ? "entry" : "entries")
                            + ", which will be deleted with it. Confirm to delete anyway."
            );
        }

        coinCatalogCommandRepository.deleteCoin(coinId);

        adminAuditRepository.recordAction(
                actor,
                AdminActionType.COIN_DELETED,
                TARGET_TYPE_COIN,
                coinId,
                describe(existing) + "; cascaded " + affectedEntries + " collection "
                        + (affectedEntries == 1 ? "entry" : "entries")
        );
    }

    private void requireActor(AdminActor actor) {
        if (actor == null) {
            throw new IllegalArgumentException("Acting administrator is required.");
        }
    }

    private static String describe(CoinCatalogView coin) {
        return coin.getCountry() + " " + coin.getDenomination() + " (" + coin.getYear() + ")";
    }

    private String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        String trimmed = value.trim();

        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    fieldName + " must be at most " + MAX_TEXT_LENGTH + " characters."
            );
        }

        return trimmed;
    }

    private int validateYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("Year is required.");
        }

        if (year <= 0 || year > MAX_YEAR) {
            throw new IllegalArgumentException("Year must be between 1 and " + MAX_YEAR + ".");
        }

        return year;
    }

    private void validateCoinId(int coinId) {
        if (coinId <= 0) {
            throw new IllegalArgumentException("Coin ID must be greater than 0.");
        }
    }
}
