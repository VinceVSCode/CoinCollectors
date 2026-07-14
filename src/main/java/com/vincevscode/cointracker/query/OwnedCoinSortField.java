// v0.3.3: Supported sort fields for owned coin screen queries.
package com.vincevscode.cointracker.query;

// Owned-coins equivalent of CoinCatalogSortField, plus QUANTITY (ce.quantity) since that
// column only exists once a collection_entries join is in play. See that enum's doc for why
// the SQL expressions are safe to splice into ORDER BY.
public enum OwnedCoinSortField {
    COIN_ID("c.id"),
    COUNTRY("c.country"),
    DENOMINATION("c.denomination"),
    YEAR("c.year"),
    QUANTITY("ce.quantity");

    private final String sqlExpression;

    OwnedCoinSortField(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    public String getSqlExpression() {
        return sqlExpression;
    }
}