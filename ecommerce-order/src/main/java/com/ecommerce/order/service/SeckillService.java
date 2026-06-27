package com.ecommerce.order.service;

import com.ecommerce.dto.response.OrderVO;

public interface SeckillService {

    OrderVO executeSeckill(Long userId, Long promotionId, Long productId, int quantity, String spec, Long addressId, String payMethod);
}