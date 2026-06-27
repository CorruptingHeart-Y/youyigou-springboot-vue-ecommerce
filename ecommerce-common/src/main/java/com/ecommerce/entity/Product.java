package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("e_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Long categoryId;
    private String coverImage;
    private String images;          // JSON数组存储多图
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String specs;           // JSON存储规格 [{name, values}]
    private String description;
    private String detail;          // HTML富文本详情
    private String keywords;
    private Integer status;         // 0-下架 1-上架

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}