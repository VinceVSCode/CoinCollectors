// v0.4.2: REST controller for catalog coin screen queries.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.PagedResponse;
import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.query.CoinCatalogSortField;
import com.vincevscode.cointracker.query.PageRequest;
import com.vincevscode.cointracker.query.SortDirection;
import com.vincevscode.cointracker.service.CoinCatalogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coins")
public class CoinCatalogController {
    private final CoinCatalogQueryService coinCatalogQueryService;

    public CoinCatalogController(CoinCatalogQueryService coinCatalogQueryService) {
        this.coinCatalogQueryService = coinCatalogQueryService;
    }

    @GetMapping
    public Object getCoins(
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "denomination", required = false) String denomination,
            @RequestParam(name = "minYear", required = false) Integer minYear,
            @RequestParam(name = "maxYear", required = false) Integer maxYear,
            @RequestParam(name = "sortField", required = false) String sortField,
            @RequestParam(name = "sortDirection", required = false) String sortDirection,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        CoinCatalogFilter filter = buildFilter(country, denomination, minYear, maxYear);
        CoinCatalogQuery query = buildQuery(filter, sortField, sortDirection, page, size);

        if (page != null && size != null) {
            return new PagedResponse<>(
                    coinCatalogQueryService.getCoins(query),
                    coinCatalogQueryService.countCoins(filter),
                    page,
                    size
            );
        }

        return coinCatalogQueryService.getCoins(query);
    }

    private CoinCatalogFilter buildFilter(
            String country,
            String denomination,
            Integer minYear,
            Integer maxYear
    ) {
        boolean noFilterProvided =
                isBlank(country)
                        && isBlank(denomination)
                        && minYear == null
                        && maxYear == null;

        if (noFilterProvided) {
            return null;
        }

        return new CoinCatalogFilter(country, denomination, minYear, maxYear);
    }

    private CoinCatalogQuery buildQuery(
            CoinCatalogFilter filter,
            String sortField,
            String sortDirection,
            Integer page,
            Integer size
    ) {
        CoinCatalogSortField parsedSortField = parseSortField(sortField);
        SortDirection parsedSortDirection = parseSortDirection(sortDirection);
        PageRequest pageRequest = buildPageRequest(page, size);

        boolean noQueryOptionsProvided =
                filter == null
                        && parsedSortField == null
                        && parsedSortDirection == null
                        && pageRequest == null;

        if (noQueryOptionsProvided) {
            return null;
        }

        return new CoinCatalogQuery(filter, parsedSortField, parsedSortDirection, pageRequest);
    }

    private CoinCatalogSortField parseSortField(String sortField) {
        if (isBlank(sortField)) {
            return null;
        }

        return CoinCatalogSortField.valueOf(sortField.trim().toUpperCase());
    }

    private SortDirection parseSortDirection(String sortDirection) {
        if (isBlank(sortDirection)) {
            return null;
        }

        return SortDirection.valueOf(sortDirection.trim().toUpperCase());
    }

    private PageRequest buildPageRequest(Integer page, Integer size) {
        if (page == null && size == null) {
            return null;
        }

        if (page == null || size == null) {
            throw new IllegalArgumentException("Both page and size must be provided together.");
        }

        return new PageRequest(page, size);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}