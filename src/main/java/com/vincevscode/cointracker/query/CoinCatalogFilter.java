// v0.4.2: Filter object for catalog coin screen queries.
package com.vincevscode.cointracker.query;

public class CoinCatalogFilter {
    private String country;
    private String denomination;
    private Integer minYear;
    private Integer maxYear;

    public CoinCatalogFilter(String country, String denomination, Integer minYear, Integer maxYear) {
        this.country = country;
        this.denomination = denomination;
        this.minYear = minYear;
        this.maxYear = maxYear;
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
}