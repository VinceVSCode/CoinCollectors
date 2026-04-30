// v0.3.0: View model for displaying missing coin data for a user-facing screen.
package com.vincevscode.cointracker.view;

import java.util.Objects;

public class MissingCoinView {
    private int coinId;
    private String country;
    private String denomination;
    private int year;

    public MissingCoinView(int coinId, String country, String denomination, int year) {
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
        return "MissingCoinView{" +
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

        MissingCoinView otherView = (MissingCoinView) otherObject;

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