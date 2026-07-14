// v0.4.2: Supported sort fields for catalog coin screen queries.
package com.vincevscode.cointracker.query;

/**
 * Closed set of sortable columns for the catalog screen — each value carries its own SQL
 * column expression rather than deriving one from the enum name, so the repository can safely
 * splice {@link #getSqlExpression()} into an ORDER BY clause: only these hard-coded strings
 * can ever reach that position, never arbitrary client input (see
 * {@link com.vincevscode.cointracker.repository.PostgresCoinCatalogQueryRepository}'s doc).
 */
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