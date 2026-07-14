// v0.4.0: REST controller for collection write operations.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.CollectionEntryResponse;
import com.vincevscode.cointracker.api.dto.SetCoinQuantityRequest;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The single write endpoint for a user's collection: set how many of a given coin they own.
 * PUT (not POST) because it's idempotent — calling it twice with the same quantity leaves the
 * same end state, matching {@link com.vincevscode.cointracker.service.CollectionTrackingService#setCoinQuantity}'s
 * upsert semantics.
 */
@RestController
@RequestMapping("/api/users/{userId}/collection")
public class CollectionCommandController {
    private final CollectionTrackingService collectionTrackingService;

    public CollectionCommandController(CollectionTrackingService collectionTrackingService) {
        this.collectionTrackingService = collectionTrackingService;
    }

    // #userId (the path variable) must match the caller's own id, unless they're an admin —
    // this is what stops user A from editing user B's collection just by changing the URL.
    @PutMapping("/{coinId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public CollectionEntryResponse setCoinQuantity(
            @PathVariable("userId") int userId,
            @PathVariable("coinId") int coinId,
            @RequestBody SetCoinQuantityRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        CollectionEntry collectionEntry =
                collectionTrackingService.setCoinQuantity(userId, coinId, request.getQuantity());

        return new CollectionEntryResponse(
                collectionEntry.getId(),
                collectionEntry.getUserId(),
                collectionEntry.getCoinId(),
                collectionEntry.getQuantity()
        );
    }
}