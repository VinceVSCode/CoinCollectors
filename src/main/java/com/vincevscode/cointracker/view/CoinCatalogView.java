// v0.4.2: View model for catalog coin screen results.
package com.vincevscode.cointracker.view;

import java.util.Objects;

public class CoinCatalogView {
    private int coinId;
    private String country;
    private String denomination;
    private int year;

    public CoinCatalogView(int coinId, String country, String denomination, int year) {
        this.coinId = coinId;
        this.country = country;
        this.denomination = denomination;
        this.year = year;
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

    @Override
    public String toString() {
        return "CoinCatalogView{" +
                "coinId=" + coinId +
                ", country='" + country + '\'' +
                ", denomination='" + denomination + '\'' +
                ", year=" + year +
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

        CoinCatalogView otherView = (CoinCatalogView) otherObject;

        return coinId == otherView.coinId
                && year == otherView.year
                && Objects.equals(country, otherView.country)
                && Objects.equals(denomination, otherView.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coinId, country, denomination, year);
    }
}