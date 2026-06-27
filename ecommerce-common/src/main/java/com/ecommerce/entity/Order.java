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
@TableName("e_order")
public class Order {

    @TableId(type = IdType.INPUT)
    private String orderNo;                 // 雪花/自定义订单号

    private Long userId;
    private Long addressId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String payMethod;               // ALIPAY, WECHAT, BALANCE
    private String payNo;                   // 第三方支付号
    private LocalDateTime payTime;
    private String status;                  // PENDING_PAY, PENDING_DELIVER, DELIVERED, COMPLETED, CANCELLED, REFUNDING, REFUNDED
    private String logisticsNo;
    private String logisticsCompany;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}