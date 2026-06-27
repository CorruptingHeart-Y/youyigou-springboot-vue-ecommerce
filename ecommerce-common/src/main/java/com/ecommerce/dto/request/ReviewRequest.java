package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private String images;

    @NotNull(message = "评分不能为空")
    @Min(1)
    @Max(5)
    private Integer rating;
}