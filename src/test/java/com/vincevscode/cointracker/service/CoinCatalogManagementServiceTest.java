// v0.9.0: Service tests for admin coin catalog management.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.repository.CoinCatalogCommandRepositoryInterface;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CoinCatalogManagementServiceTest {

    private static final AdminActor ACTOR = new AdminActor(1, "vince");

    private CoinCatalogCommandRepositoryInterface repository;
    private AdminAuditRepositoryInterface auditRepository;
    private CoinCatalogManagementService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CoinCatalogCommandRepositoryInterface.class);
        auditRepository = Mockito.mock(AdminAuditRepositoryInterface.class);
        service = new CoinCatalogManagementService(repository, auditRepository);
    }

    @Test
    void createCoin_shouldTrimTextAndDelegateToRepository() {
        when(repository.createCoin("Bulgaria", "1 Lev", 2002))
                .thenReturn(new CoinCatalogView(7, "Bulgaria", "1 Lev", 2002));

        CoinCatalogView created = service.createCoin(ACTOR, "  Bulgaria  ", "  1 Lev  ", 2002);

        assertEquals(7, created.getCoinId());
        verify(repository).createCoin("Bulgaria", "1 Lev", 2002);
    }

    @Test
    void createCoin_shouldRejectMissingOrBlankText() {
        assertEquals("Country is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, null, "1 Lev", 2002)).getMessage());

        assertEquals("Country is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, "   ", "1 Lev", 2002)).getMessage());

        assertEquals("Denomination is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, "Bulgaria", " ", 2002)).getMessage());
    }

    @Test
    void createCoin_shouldRejectOverlongText() {
        String tooLong = "x".repeat(101);

        assertEquals("Country must be at most 100 characters.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, tooLong, "1 Lev", 2002)).getMessage());
    }

    @Test
    void createCoin_shouldRejectMissingOrOutOfRangeYear() {
        assertEquals("Year is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, "Bulgaria", "1 Lev", null)).getMessage());

        assertEquals("Year must be between 1 and 2999.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, "Bulgaria", "1 Lev", 0)).getMessage());

        assertEquals("Year must be between 1 and 2999.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(ACTOR, "Bulgaria", "1 Lev", 3000)).getMessage());
    }

    @Test
    void updateCoin_shouldReturnUpdatedCoin() {
        when(repository.updateCoin(1, "Germany", "2 Euro", 2011))
                .thenReturn(new CoinCatalogView(1, "Germany", "2 Euro", 2011));

        CoinCatalogView updated = service.updateCoin(ACTOR, 1, "Germany", "2 Euro", 2011);

        assertEquals("2 Euro", updated.getDenomination());
    }

    @Test
    void updateCoin_shouldRejectUnknownCoin() {
        when(repository.updateCoin(anyInt(), Mockito.anyString(), Mockito.anyString(), anyInt()))
                .thenReturn(null);

        assertEquals("Coin was not found.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.updateCoin(ACTOR, 99, "Germany", "2 Euro", 2011)).getMessage());
    }

    @Test
    void deleteCoin_shouldDeleteWhenNoCollectionEntriesReferenceIt() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(0L);

        service.deleteCoin(ACTOR, 1, false);

        verify(repository).deleteCoin(1);
    }

    @Test
    void deleteCoin_shouldRejectUnknownCoin() {
        when(repository.findCoinById(99)).thenReturn(null);

        assertEquals("Coin was not found.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.deleteCoin(ACTOR, 99, false)).getMessage());

        verify(repository, never()).deleteCoin(anyInt());
    }

    // The FK is ON DELETE CASCADE, so without this guard deleting a referenced coin would
    // silently destroy other users' collection entries rather than failing loudly.
    @Test
    void deleteCoin_shouldRefuseWhenCollectionEntriesWouldCascadeAway() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(3L);

        assertEquals(
                "This coin is in 3 collection entries, which will be deleted with it. Confirm to delete anyway.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.deleteCoin(ACTOR, 1, false)).getMessage()
        );

        verify(repository, never()).deleteCoin(anyInt());
    }

    @Test
    void deleteCoin_shouldSingularizeMessageForOneEntry() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(1L);

        assertEquals(
                "This coin is in 1 collection entry, which will be deleted with it. Confirm to delete anyway.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.deleteCoin(ACTOR, 1, false)).getMessage()
        );
    }

    @Test
    void deleteCoin_shouldDeleteReferencedCoinWhenForced() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(3L);

        service.deleteCoin(ACTOR, 1, true);

        verify(repository).deleteCoin(1);
    }

    // Forcing still counts the entries — not to gate the delete, but because how much of other
    // users' data this destroyed is the single most important thing the record has to preserve.
    @Test
    void deleteCoin_shouldRecordHowManyCollectionEntriesCascadedWhenForced() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(3L);

        service.deleteCoin(ACTOR, 1, true);

        verify(auditRepository).recordAction(
                ACTOR,
                AdminActionType.COIN_DELETED,
                "COIN",
                1,
                "Bulgaria 1 Lev (2002); cascaded 3 collection entries"
        );
    }

    @Test
    void deleteCoin_shouldRecordSingularCascadeWording() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(1L);

        service.deleteCoin(ACTOR, 1, true);

        verify(auditRepository).recordAction(
                ACTOR,
                AdminActionType.COIN_DELETED,
                "COIN",
                1,
                "Bulgaria 1 Lev (2002); cascaded 1 collection entry"
        );
    }

    @Test
    void createCoin_shouldRecordAuditEntry() {
        when(repository.createCoin("Bulgaria", "1 Lev", 2002))
                .thenReturn(new CoinCatalogView(7, "Bulgaria", "1 Lev", 2002));

        service.createCoin(ACTOR, "Bulgaria", "1 Lev", 2002);

        verify(auditRepository).recordAction(
                ACTOR, AdminActionType.COIN_CREATED, "COIN", 7, "Bulgaria 1 Lev (2002)");
    }

    @Test
    void updateCoin_shouldRecordAuditEntry() {
        when(repository.updateCoin(1, "Germany", "2 Euro", 2011))
                .thenReturn(new CoinCatalogView(1, "Germany", "2 Euro", 2011));

        service.updateCoin(ACTOR, 1, "Germany", "2 Euro", 2011);

        verify(auditRepository).recordAction(
                ACTOR, AdminActionType.COIN_UPDATED, "COIN", 1, "Germany 2 Euro (2011)");
    }

    @Test
    void deleteCoin_shouldNotRecordAnythingWhenRefused() {
        when(repository.findCoinById(1)).thenReturn(new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002));
        when(repository.countCollectionEntriesForCoin(1)).thenReturn(2L);

        assertThrows(IllegalArgumentException.class, () -> service.deleteCoin(ACTOR, 1, false));

        verifyNoInteractions(auditRepository);
    }

    @Test
    void createCoin_shouldRejectMissingActor() {
        assertEquals(
                "Acting administrator is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.createCoin(null, "Bulgaria", "1 Lev", 2002)).getMessage()
        );

        verifyNoInteractions(auditRepository);
    }

    @Test
    void deleteCoin_shouldRejectNonPositiveCoinId() {
        assertEquals("Coin ID must be greater than 0.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.deleteCoin(ACTOR, 0, false)).getMessage());
    }
}
