package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalSales;
    private Long todayOrders;
    private BigDecimal todaySales;
    private Map<String, Long> orderStatusCount;
    private List<ProductRankVO> hotProducts;
    private List<SaleTrendVO> saleTrend;
}