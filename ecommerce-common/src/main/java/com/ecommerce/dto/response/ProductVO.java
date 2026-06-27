package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String coverImage;
    private String[] images;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String specs;
    private String description;
    private String detail;
    private String keywords;
    private Integer status;
    private Boolean isFavorited;
    private LocalDateTime createTime;
}