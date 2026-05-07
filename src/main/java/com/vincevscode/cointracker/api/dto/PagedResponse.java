// v0.4.0: Generic paged response DTO for frontend-friendly list endpoints.
package com.vincevscode.cointracker.api.dto;

import java.util.List;

public class PagedResponse<T> {
    private List<T> items;
    private long totalCount;
    private int pageNumber;
    private int pageSize;

    public PagedResponse(List<T> items, long totalCount, int pageNumber, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }
}