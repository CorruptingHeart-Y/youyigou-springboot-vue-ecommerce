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
@TableName("e_promotion")
public class Promotion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String type;            // SECKILL, DISCOUNT, COUPON
    private String description;
    private Long productId;         // NULL表示全场
    private BigDecimal discountValue;
    private BigDecimal minAmount;    // 最低消费金额（COUPON类型）
    private Integer seckillStock;   // 秒杀库存（SECKILL类型）
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;         // 0-停用 1-启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String coverImage;     // 关联商品的封面图（非数据库字段，查询时填充）
    @TableField(exist = false)
    private Integer totalStock;    // 秒杀总库存（seckillStock + 已售，前端进度条用）
    @TableField(exist = false)
    private Integer soldCount;     // 已售数量（前端进度条用）
}