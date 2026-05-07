// v0.4.0: REST controller for collection write operations.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.CollectionEntryResponse;
import com.vincevscode.cointracker.api.dto.SetCoinQuantityRequest;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/collection")
public class CollectionCommandController {
    private final CollectionTrackingService collectionTrackingService;

    public CollectionCommandController(CollectionTrackingService collectionTrackingService) {
        this.collectionTrackingService = collectionTrackingService;
    }

    @PutMapping("/{coinId}")
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