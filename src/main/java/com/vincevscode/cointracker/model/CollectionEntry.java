// v0.3.0: Collection entry model linking a user to a coin with owned quantity.
package com.vincevscode.cointracker.model;

import java.util.Objects;

public class CollectionEntry {
    private int id;
    private int userId;
    private int coinId;
    private int quantity;

    public CollectionEntry(int id, int userId, int coinId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.coinId = coinId;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
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

    @Override
    public String toString() {
        return "CollectionEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", coinId=" + coinId +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        CollectionEntry otherEntry = (CollectionEntry) otherObject;

        return id == otherEntry.id
                && userId == otherEntry.userId
                && coinId == otherEntry.coinId
                && quantity == otherEntry.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, coinId, quantity);
    }
}