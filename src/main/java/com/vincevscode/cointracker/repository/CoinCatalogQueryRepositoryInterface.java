// v0.4.2: Repository contract for catalog coin screen queries.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.view.CoinCatalogView;

import java.util.List;

/**
 * Read-side contract for the paged/filterable/sortable catalog browse screen
 * ({@code GET /api/coins}). Separate from {@link CoinRepositoryInterface} because catalog
 * browsing needs query/paging semantics that plain CRUD doesn't, and keeping them apart lets
 * the write path stay simple.
 */
public interface CoinCatalogQueryRepositoryInterface {
    List<CoinCatalogView> getCoins(CoinCatalogQuery query);

    long countCoins(CoinCatalogFilter filter);
}