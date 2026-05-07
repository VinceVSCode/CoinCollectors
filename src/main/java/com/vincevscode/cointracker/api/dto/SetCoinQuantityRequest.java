// v0.4.0: Request DTO for setting a user's quantity for a coin.
package com.vincevscode.cointracker.api.dto;

public class SetCoinQuantityRequest {
    private int quantity;

    public SetCoinQuantityRequest() {
    }

    public SetCoinQuantityRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}