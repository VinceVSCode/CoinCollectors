// v0.3.3: Query object for missing coin screen requests.
package com.vincevscode.cointracker.query;

public class MissingCoinQuery {
    private MissingCoinFilter filter;
    private MissingCoinSortField sortField;
    private SortDirection sortDirection;
    private PageRequest pageRequest;

    public MissingCoinQuery(
            MissingCoinFilter filter,
            MissingCoinSortField sortField,
            SortDirection sortDirection,
            PageRequest pageRequest
    ) {
        this.filter = filter;
        this.sortField = sortField;
        this.sortDirection = sortDirection;
        this.pageRequest = pageRequest;
    }

    public MissingCoinFilter getFilter() {
        return filter;
    }

    public MissingCoinSortField getSortField() {
        return sortField;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }
}
