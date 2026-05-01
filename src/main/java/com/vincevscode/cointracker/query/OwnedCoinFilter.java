// v0.3.3: Filter object for owned coin screen queries.
package com.vincevscode.cointracker.query;

public class OwnedCoinFilter {
    private String country;
    private String denomination;
    private Integer minYear;
    private Integer maxYear;
    private Integer minQuantity;

    public OwnedCoinFilter(String country, String denomination, Integer minYear, Integer maxYear, Integer minQuantity) {
        this.country = country;
        this.denomination = denomination;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.minQuantity = minQuantity;
    }

    public String getCountry() {
        return country;
    }

    public String getDenomination() {
        return denomination;
    }

    public Integer getMinYear() {
        return minYear;
    }

    public Integer getMaxYear() {
        return maxYear;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }
}