package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private String orderNo;
    private Long userId;
    private String username;
    private String addressDetail;
    private String receiverName;
    private String receiverPhone;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String payMethod;
    private LocalDateTime payTime;
    private String status;
    private String logisticsNo;
    private String logisticsCompany;
    private String remark;
    private List<OrderItemVO> items;
    private LocalDateTime createTime;
}