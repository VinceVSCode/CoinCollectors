// v0.3.3: Pagination request for screen-oriented queries.
package com.vincevscode.cointracker.query;

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