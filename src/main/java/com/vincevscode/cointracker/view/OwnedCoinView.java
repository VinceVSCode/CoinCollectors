// v0.3.0: View model for displaying owned coin data for a user-facing screen.
package com.vincevscode.cointracker.view;

import java.util.Objects;

public class OwnedCoinView {
    private int coinId;
    private String country;
    private String denomination;
    private int year;
    private int quantity;

    public OwnedCoinView(int coinId, String country, String denomination, int year, int quantity) {
        this.coinId = coinId;
        this.country = country;
        this.denomination = denomination;
        this.year = year;
        this.quantity = quantity;
    }

    public int getCoinId() {
        return coinId;
    }

    public String getCountry() {
        return country;
    }

    public String getDenomination() {
        return denomination;
    }

    public int getYear() {
        return year;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "OwnedCoinView{" +
                "coinId=" + coinId +
                ", country='" + country + '\'' +
                ", denomination='" + denomination + '\'' +
                ", year=" + year +
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

        OwnedCoinView otherView = (OwnedCoinView) otherObject;

        return coinId == otherView.coinId
                && year == otherView.year
                && quantity == otherView.quantity
                && Objects.equals(country, otherView.country)
                && Objects.equals(denomination, otherView.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coinId, country, denomination, year, quantity);
    }
}
