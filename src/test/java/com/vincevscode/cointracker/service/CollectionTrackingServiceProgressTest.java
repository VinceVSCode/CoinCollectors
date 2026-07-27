// v0.7.0: Unit tests for CollectionTrackingService.getCollectionProgress calculation logic.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.view.CollectionProgressView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollectionTrackingServiceProgressTest {
    private CollectionEntryRepositoryInterface repository;
    private CollectionTrackingService service;

    @BeforeEach
    void setUp() {
        repository = mock(CollectionEntryRepositoryInterface.class);
        service = new CollectionTrackingService(repository);
    }

    // isNull() matches the `filter` argument getCollectionProgress always passes as null —
    // it computes totals from the unfiltered owned/missing counts, never a caller-supplied filter.
    private void stubCounts(long owned, long missing) {
        when(repository.countOwnedCoinsForUser(eq(1), isNull())).thenReturn(owned);
        when(repository.countMissingCoinsForUser(eq(1), isNull())).thenReturn(missing);
    }

    @Test
    void getCollectionProgress_shouldReportZeroPercentWhenCatalogIsEmpty() {
        stubCounts(0, 0);

        CollectionProgressView progress = service.getCollectionProgress(1);

        assertEquals(new CollectionProgressView(1, 0, 0, 0, 0.0), progress);
    }

    @Test
    void getCollectionProgress_shouldReportFullCompletionWhenNothingIsMissing() {
        stubCounts(6, 0);

        CollectionProgressView progress = service.getCollectionProgress(1);

        assertEquals(new CollectionProgressView(1, 6, 6, 0, 100.0), progress);
    }

    @Test
    void getCollectionProgress_shouldRoundPercentageToOneDecimal() {
        // 2 of 6 owned -> 33.333...% -> 33.3%
        stubCounts(2, 4);

        CollectionProgressView progress = service.getCollectionProgress(1);

        assertEquals(new CollectionProgressView(1, 6, 2, 4, 33.3), progress);
    }

    @Test
    void getCollectionProgress_shouldReportHalfway() {
        stubCounts(3, 3);

        CollectionProgressView progress = service.getCollectionProgress(1);

        assertEquals(new CollectionProgressView(1, 6, 3, 3, 50.0), progress);
    }

    @Test
    void getCollectionProgress_shouldRejectInvalidUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getCollectionProgress(0)
        );
    }
}
