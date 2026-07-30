// v0.4.0: REST controller for user collection screen queries.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.MissingCoinSortField;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinSortField;
import com.vincevscode.cointracker.query.PageRequest;
import com.vincevscode.cointracker.query.SortDirection;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import com.vincevscode.cointracker.view.CollectionProgressView;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The three read endpoints for a user's own collection view: what they own, what they're
 * missing from the catalog, and their completion progress. All three share the same
 * owner-or-admin @PreAuthorize rule as {@link CollectionCommandController}. The owned/missing
 * endpoints follow the same "Object return type: paged envelope vs plain list" pattern as
 * {@link CoinCatalogController#getCoins} — see that class's comment for why.
 */
@RestController
@RequestMapping("/api/users/{userId}")
public class CollectionQueryController {
    private final CollectionTrackingService collectionTrackingService;

    public CollectionQueryController(CollectionTrackingService collectionTrackingService) {
        this.collectionTrackingService = collectionTrackingService;
    }

    @GetMapping("/owned-coins")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public Object getOwnedCoinsForUser(
            @PathVariable("userId") int userId,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "denomination", required = false) String denomination,
            @RequestParam(name = "minYear", required = false) Integer minYear,
            @RequestParam(name = "maxYear", required = false) Integer maxYear,
            @RequestParam(name = "minQuantity", required = false) Integer minQuantity,
            @RequestParam(name = "sortField", required = false) String sortField,
            @RequestParam(name = "sortDirection", required = false) String sortDirection,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        OwnedCoinFilter filter = buildOwnedCoinFilter(
                country,
                denomination,
                minYear,
                maxYear,
                minQuantity
        );

        OwnedCoinQuery query = buildOwnedCoinQuery(filter, sortField, sortDirection, page, size);

        if (page != null && size != null) {
            return new com.vincevscode.cointracker.api.dto.PagedResponse<>(
                    collectionTrackingService.getOwnedCoinsForUser(userId, query),
                    collectionTrackingService.countOwnedCoinsForUser(userId, filter),
                    page,
                    size
            );
        }

        return collectionTrackingService.getOwnedCoinsForUser(userId, query);
    }

    @GetMapping("/missing-coins")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public Object getMissingCoinsForUser(
            @PathVariable("userId") int userId,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "denomination", required = false) String denomination,
            @RequestParam(name = "minYear", required = false) Integer minYear,
            @RequestParam(name = "maxYear", required = false) Integer maxYear,
            @RequestParam(name = "sortField", required = false) String sortField,
            @RequestParam(name = "sortDirection", required = false) String sortDirection,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        MissingCoinFilter filter = buildMissingCoinFilter(
                country,
                denomination,
                minYear,
                maxYear
        );

        MissingCoinQuery query = buildMissingCoinQuery(filter, sortField, sortDirection, page, size);

        if (page != null && size != null) {
            return new com.vincevscode.cointracker.api.dto.PagedResponse<>(
                    collectionTrackingService.getMissingCoinsForUser(userId, query),
                    collectionTrackingService.countMissingCoinsForUser(userId, filter),
                    page,
                    size
            );
        }

        return collectionTrackingService.getMissingCoinsForUser(userId, query);
    }

    @GetMapping("/progress")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public CollectionProgressView getCollectionProgress(@PathVariable("userId") int userId) {
        return collectionTrackingService.getCollectionProgress(userId);
    }

    /**
     * Exports the user's owned coins as a CSV download. Unlike the JSON read endpoints this one
     * is deliberately unfiltered and unpaged — an export is a snapshot of the whole collection,
     * and a partial file would be a surprising thing to hand someone as a backup.
     */
    @GetMapping(value = "/collection/export", produces = "text/csv")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public ResponseEntity<String> exportCollectionAsCsv(@PathVariable("userId") int userId) {
        String csv = CollectionCsvFormatter.toCsv(
                collectionTrackingService.getOwnedCoinsForUser(userId)
        );

        return ResponseEntity.ok()
                // Charset is explicit so non-ASCII country names survive the round-trip into a
                // spreadsheet, which would otherwise guess an encoding.
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"collection-" + userId + ".csv\""
                )
                .body(csv);
    }

    private OwnedCoinFilter buildOwnedCoinFilter(
            String country,
            String denomination,
            Integer minYear,
            Integer maxYear,
            Integer minQuantity
    ) {
        boolean noFilterProvided =
                isBlank(country)
                        && isBlank(denomination)
                        && minYear == null
                        && maxYear == null
                        && minQuantity == null;

        if (noFilterProvided) {
            return null;
        }

        return new OwnedCoinFilter(country, denomination, minYear, maxYear, minQuantity);
    }

    private MissingCoinFilter buildMissingCoinFilter(
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

        return new MissingCoinFilter(country, denomination, minYear, maxYear);
    }

    private OwnedCoinQuery buildOwnedCoinQuery(
            OwnedCoinFilter filter,
            String sortField,
            String sortDirection,
            Integer page,
            Integer size
    ) {
        OwnedCoinSortField parsedSortField = parseOwnedCoinSortField(sortField);
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

        return new OwnedCoinQuery(filter, parsedSortField, parsedSortDirection, pageRequest);
    }

    private MissingCoinQuery buildMissingCoinQuery(
            MissingCoinFilter filter,
            String sortField,
            String sortDirection,
            Integer page,
            Integer size
    ) {
        MissingCoinSortField parsedSortField = parseMissingCoinSortField(sortField);
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

        return new MissingCoinQuery(filter, parsedSortField, parsedSortDirection, pageRequest);
    }

    private OwnedCoinSortField parseOwnedCoinSortField(String sortField) {
        if (isBlank(sortField)) {
            return null;
        }

        return OwnedCoinSortField.valueOf(sortField.trim().toUpperCase());
    }

    private MissingCoinSortField parseMissingCoinSortField(String sortField) {
        if (isBlank(sortField)) {
            return null;
        }

        return MissingCoinSortField.valueOf(sortField.trim().toUpperCase());
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