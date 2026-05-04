// v0.3.3: Supported sort fields for owned coin screen queries.
package com.vincevscode.cointracker.query;

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