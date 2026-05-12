// v0.4.2: Query object for catalog coin screen requests.
package com.vincevscode.cointracker.query;

public class CoinCatalogQuery {
    private CoinCatalogFilter filter;
    private CoinCatalogSortField sortField;
    private SortDirection sortDirection;
    private PageRequest pageRequest;

    public CoinCatalogQuery(
            CoinCatalogFilter filter,
            CoinCatalogSortField sortField,
            SortDirection sortDirection,
            PageRequest pageRequest
    ) {
        this.filter = filter;
        this.sortField = sortField;
        this.sortDirection = sortDirection;
        this.pageRequest = pageRequest;
    }

    public CoinCatalogFilter getFilter() {
        return filter;
    }

    public CoinCatalogSortField getSortField() {
        return sortField;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }
}