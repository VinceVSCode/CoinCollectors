// v0.3.0: Supported sort fields for missing coin screen queries.
package com.vincevscode.cointracker.query;

// Missing-coins equivalent of CoinCatalogSortField (no QUANTITY option — a missing coin has
// no owned quantity to sort by). See that enum's doc for the ORDER BY injection-safety note.
public enum MissingCoinSortField {
    COIN_ID("c.id"),
    COUNTRY("c.country"),
    DENOMINATION("c.denomination"),
    YEAR("c.year");

    private final String sqlExpression;

    MissingCoinSortField(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    public String getSqlExpression() {
        return sqlExpression;
    }
}