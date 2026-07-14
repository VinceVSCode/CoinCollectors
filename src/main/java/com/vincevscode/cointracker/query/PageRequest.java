// v0.3.3: Pagination request for screen-oriented queries.
package com.vincevscode.cointracker.query;

/**
 * 1-indexed page + size, shared by every paged query (catalog, owned, missing coins).
 * {@link #getOffset()} converts to the 0-based SQL OFFSET the repositories bind directly into
 * {@code LIMIT ? OFFSET ?} — 1-indexing is purely for a friendlier API/URL (?page=1), not a
 * quirk of the storage layer.
 */
public class PageRequest {
    private int pageNumber;
    private int pageSize;

    public PageRequest(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }
}