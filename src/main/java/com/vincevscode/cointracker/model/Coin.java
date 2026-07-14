package com.vincevscode.cointracker.model;

import java.util.Objects;

/**
 * A single catalog entry: one coin design/mint identified by country + denomination + year.
 * This is the "thing that can be collected" — {@link CollectionEntry} links a {@link User}
 * to a Coin with a quantity owned. Coin itself carries no ownership data.
 */
public class Coin {
    private int id;
    private String  country;
    private String denomination;
    private int year;

    public Coin(int id, String country, String denomination, int year){
    this.id = id;
    this.country = country;
    this.denomination = denomination;
    this.year = year;
    }

    public int getId() {
        return id;
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
    public String toString(){
        return "Coin{" +
                "id=" + id +
                ", country='" + country + '\'' +
                ", denomination='" + denomination + '\'' +
                ", year=" + year +
                '}';
    }

    // equals/hashCode are value-based (all fields) rather than id-only, since Coin instances
    // are sometimes compared before an id has been assigned (e.g. catalog import/dedup checks).
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        Coin otherCoin = (Coin) otherObject;

        return id == otherCoin.id
                && year == otherCoin.year
                && Objects.equals(country, otherCoin.country)
                && Objects.equals(denomination, otherCoin.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, country, denomination, year);
    }
}
