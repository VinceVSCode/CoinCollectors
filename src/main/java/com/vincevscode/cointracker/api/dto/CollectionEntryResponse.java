// v0.4.0: Response DTO for collection entry write operations.
package com.vincevscode.cointracker.api.dto;

// Response shape for CollectionCommandController#setCoinQuantity — mirrors CollectionEntry's
// fields but as an explicit API contract, decoupled from the domain model so the model can
// change shape without silently changing the wire format.
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