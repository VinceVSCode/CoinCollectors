// v0.3.0: Filter object for missing coin screen queries.
package com.vincevscode.cointracker.query;

public class MissingCoinFilter {
    private String country;
    private String denomination;
    private Integer minYear;
    private Integer maxYear;

    public MissingCoinFilter(String country, String denomination, Integer minYear, Integer maxYear) {
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