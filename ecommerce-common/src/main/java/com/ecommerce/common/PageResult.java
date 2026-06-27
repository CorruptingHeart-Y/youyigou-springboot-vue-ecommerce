package com.ecommerce.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private long page;
    private long size;
    private long totalPages;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        long totalPages = size > 0 ? (total + size - 1) / size : 0;
        return new PageResult<>(total, page, size, totalPages, records);
    }
}