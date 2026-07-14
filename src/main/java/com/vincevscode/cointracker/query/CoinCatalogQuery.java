// v0.4.2: Query object for catalog coin screen requests.
package com.vincevscode.cointracker.query;

/**
 * Bundles filter + sort + paging for one catalog request so the controller/service/repository
 * only need to pass a single object around instead of four. Any of the four fields may be
 * null, meaning "use the default" (see the repository's fallback to {@code c.id ASC}, unpaged).
 * {@link OwnedCoinQuery}/{@link MissingCoinQuery} are the structurally identical equivalents
 * for the other two screens — kept as distinct types rather than one generic class because
 * their filter/sort-field types differ.
 */
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