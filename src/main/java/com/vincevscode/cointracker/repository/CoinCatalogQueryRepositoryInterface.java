// v0.4.2: Repository contract for catalog coin screen queries.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.view.CoinCatalogView;

import java.util.List;

public interface CoinCatalogQueryRepositoryInterface {
    List<CoinCatalogView> getCoins(CoinCatalogQuery query);

    long countCoins(CoinCatalogFilter filter);
}