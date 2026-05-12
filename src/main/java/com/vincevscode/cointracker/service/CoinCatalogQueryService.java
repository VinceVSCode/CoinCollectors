// v0.4.2: Service layer for catalog coin screen queries.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.query.PageRequest;
import com.vincevscode.cointracker.repository.CoinCatalogQueryRepositoryInterface;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class CoinCatalogQueryService {
    private final CoinCatalogQueryRepositoryInterface coinCatalogQueryRepository;

    public CoinCatalogQueryService(CoinCatalogQueryRepositoryInterface coinCatalogQueryRepository) {
        this.coinCatalogQueryRepository = coinCatalogQueryRepository;
    }

    @Transactional(readOnly = true)
    public List<CoinCatalogView> getCoins(CoinCatalogQuery query) {
        validateCoinCatalogQuery(query);
        return coinCatalogQueryRepository.getCoins(query);
    }

    @Transactional(readOnly = true)
    public long countCoins(CoinCatalogFilter filter) {
        validateCoinCatalogFilter(filter);
        return coinCatalogQueryRepository.countCoins(filter);
    }

    private void validateCoinCatalogQuery(CoinCatalogQuery query) {
        if (query == null) {
            return;
        }

        validateCoinCatalogFilter(query.getFilter());
        validatePageRequest(query.getPageRequest());
    }

    private void validateCoinCatalogFilter(CoinCatalogFilter filter) {
        if (filter == null) {
            return;
        }

        if (filter.getMinYear() != null && filter.getMinYear() <= 0) {
            throw new IllegalArgumentException("Minimum year must be greater than 0.");
        }

        if (filter.getMaxYear() != null && filter.getMaxYear() <= 0) {
            throw new IllegalArgumentException("Maximum year must be greater than 0.");
        }

        if (filter.getMinYear() != null
                && filter.getMaxYear() != null
                && filter.getMinYear() > filter.getMaxYear()) {
            throw new IllegalArgumentException("Minimum year cannot be greater than maximum year.");
        }
    }

    private void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            return;
        }

        if (pageRequest.getPageNumber() <= 0) {
            throw new IllegalArgumentException("Page number must be greater than 0.");
        }

        if (pageRequest.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }
    }
}