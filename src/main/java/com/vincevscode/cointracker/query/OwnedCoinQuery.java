// v0.3.0: Query object for owned coin screen requests.
package com.vincevscode.cointracker.query;

public class OwnedCoinQuery {
    private OwnedCoinFilter filter;
    private OwnedCoinSortField sortField;
    private SortDirection sortDirection;
    private PageRequest pageRequest;

    public OwnedCoinQuery(
            OwnedCoinFilter filter,
            OwnedCoinSortField sortField,
            SortDirection sortDirection,
            PageRequest pageRequest
    ) {
        this.filter = filter;
        this.sortField = sortField;
        this.sortDirection = sortDirection;
        this.pageRequest = pageRequest;
    }

    public OwnedCoinFilter getFilter() {
        return filter;
    }

    public OwnedCoinSortField getSortField() {
        return sortField;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }
}