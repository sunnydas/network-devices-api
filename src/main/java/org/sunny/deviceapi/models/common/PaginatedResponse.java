package org.sunny.deviceapi.models.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private long totalPages;
    private boolean hasNextPage;
}