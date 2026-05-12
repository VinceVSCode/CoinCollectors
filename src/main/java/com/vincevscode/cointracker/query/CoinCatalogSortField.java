// v0.4.2: Supported sort fields for catalog coin screen queries.
package com.vincevscode.cointracker.query;

public enum CoinCatalogSortField {
    COIN_ID("c.id"),
    COUNTRY("c.country"),
    DENOMINATION("c.denomination"),
    YEAR("c.year");

    private final String sqlExpression;

    CoinCatalogSortField(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    public String getSqlExpression() {
        return sqlExpression;
    }
}