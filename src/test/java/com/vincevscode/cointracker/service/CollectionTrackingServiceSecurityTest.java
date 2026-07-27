// v0.7.2: Adversarial/robustness tests for CollectionTrackingService write path.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollectionTrackingServiceSecurityTest {
    private CollectionEntryRepositoryInterface repository;
    private CollectionTrackingService service;

    @BeforeEach
    void setUp() {
        repository = mock(CollectionEntryRepositoryInterface.class);
        service = new CollectionTrackingService(repository);
    }

    @Test
    void setCoinQuantity_shouldReturnCleanDomainErrorWhenCoinDoesNotExist() {
        // No existing entry, and the insert fails the coin_id foreign key constraint.
        when(repository.findCollectionEntryByUserIdAndCoinId(1, 99999)).thenReturn(null);
        when(repository.addCollectionEntry(1, 99999, 1))
                .thenThrow(new DataIntegrityViolationException("FK violation on coin_id"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, 99999, 1)
        );

        // Must be a clean, non-leaking domain message (previously surfaced as a raw 500).
        assertEquals("Coin was not found.", exception.getMessage());
    }

    @Test
    void setCoinQuantity_shouldRejectNonPositiveCoinIdBeforeTouchingTheRepository() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, 0, 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, -5, 1)
        );
    }

    @Test
    void setCoinQuantity_shouldRejectNegativeQuantity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, 1, -1)
        );

        assertEquals("Quantity cannot be negative.", exception.getMessage());
    }

    @Test
    void setCoinQuantity_shouldRejectNonPositiveUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(0, 1, 1)
        );
    }
}
