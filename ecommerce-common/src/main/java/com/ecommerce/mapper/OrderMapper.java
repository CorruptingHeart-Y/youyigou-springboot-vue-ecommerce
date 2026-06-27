package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(*) FROM e_order WHERE deleted = 0")
    Long countTotalOrders();

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM e_order WHERE deleted = 0 AND pay_time IS NOT NULL")
    BigDecimal sumTotalSales();

    @Select("SELECT COUNT(*) FROM e_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    Long countTodayOrders();

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM e_order WHERE deleted = 0 AND pay_time IS NOT NULL AND DATE(pay_time) = CURDATE()")
    BigDecimal sumTodaySales();

    @Select("SELECT status, COUNT(*) as cnt FROM e_order WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT p.id as product_id, p.name as product_name, SUM(oi.quantity) as sales_count, SUM(oi.subtotal) as sales_amount " +
            "FROM e_order_item oi JOIN e_product p ON oi.product_id = p.id " +
            "WHERE p.deleted = 0 GROUP BY p.id, p.name ORDER BY sales_count DESC LIMIT 5")
    List<Map<String, Object>> topSaleProducts();

    @Select("SELECT DATE(pay_time) as date, COALESCE(SUM(pay_amount), 0) as amount, COUNT(*) as order_count " +
            "FROM e_order WHERE deleted = 0 AND pay_time IS NOT NULL AND pay_time >= DATE_SUB(CURDATE(), INTERVAL 14 DAY) " +
            "GROUP BY DATE(pay_time) ORDER BY date")
    List<Map<String, Object>> dailySaleTrend();
}