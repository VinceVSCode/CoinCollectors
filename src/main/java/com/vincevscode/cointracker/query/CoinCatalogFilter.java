// v0.4.2: Filter object for catalog coin screen queries.
package com.vincevscode.cointracker.query;

/**
 * Optional filter criteria for {@code GET /api/coins}, translated from query-string params in
 * {@link com.vincevscode.cointracker.api.CoinCatalogController}. Every field is nullable/blank
 * meaning "don't filter on this" — {@link com.vincevscode.cointracker.repository.PostgresCoinCatalogQueryRepository}
 * only appends a SQL condition for fields that are actually set. Compare
 * {@link OwnedCoinFilter} (adds minQuantity) and {@link MissingCoinFilter} (same shape as
 * this one) for the other two screens' equivalents.
 */
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