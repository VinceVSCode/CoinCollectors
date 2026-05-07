// v0.4.0: Response DTO for collection entry write operations.
package com.vincevscode.cointracker.api.dto;

public class CollectionEntryResponse {
    private int entryId;
    private int userId;
    private int coinId;
    private int quantity;

    public CollectionEntryResponse(int entryId, int userId, int coinId, int quantity) {
        this.entryId = entryId;
        this.userId = userId;
        this.coinId = coinId;
        this.quantity = quantity;
    }

    public int getEntryId() {
        return entryId;
    }

    public int getUserId() {
        return userId;
    }

    public int getCoinId() {
        return coinId;
    }

    public int getQuantity() {
        return quantity;
    }
}