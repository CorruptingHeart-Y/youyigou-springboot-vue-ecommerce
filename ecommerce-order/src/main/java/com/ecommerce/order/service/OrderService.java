package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService extends IService<Order> {

    OrderVO createOrder(Long userId, Long addressId, String payMethod, String remark);

    OrderVO createOrder(Long userId, Long addressId, String payMethod, String remark, Long couponId, BigDecimal couponDiscount);

    void payOrder(String orderNo, String payMethod);

    OrderVO getOrderDetail(String orderNo);

    Page<OrderVO> listUserOrders(Long userId, int page, int size, String status);

    Page<OrderVO> listAllOrders(int page, int size, String orderNo, String status);

    void cancelOrder(Long userId, String orderNo);

    void confirmReceive(Long userId, String orderNo);

    void refundOrder(Long userId, String orderNo);

    void adminCancelOrder(String orderNo);

    void adminDeliverOrder(String orderNo, String logisticsNo, String logisticsCompany);

    void adminProcessRefund(String orderNo, boolean approve);

    void exportOrdersToExcel(String filePath);
}