package com.ecommerce.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.OrderItemVO;
import com.ecommerce.dto.response.OrderVO;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.*;
import com.ecommerce.order.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final PromotionMapper promotionMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public OrderVO executeSeckill(Long userId, Long promotionId, Long productId, int quantity, String spec, Long addressId, String payMethod) {
        Promotion promo = promotionMapper.selectById(promotionId);
        if (promo == null || promo.getStatus() != 1 || !"SECKILL".equals(promo.getType())) {
            throw new BusinessException("秒杀活动不存在或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promo.getStartTime()) || now.isAfter(promo.getEndTime())) {
            throw new BusinessException("秒杀活动不在有效时间内");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }

        // Redis atomic stock deduction
        String stockKey = "seckill:stock:" + promotionId;
        Long remaining;
        try {
            Boolean hasKey = redisTemplate.hasKey(stockKey);
            if (Boolean.FALSE.equals(hasKey)) {
                int stock = promo.getSeckillStock() != null ? promo.getSeckillStock() : product.getStock();
                long ttl = Duration.between(now, promo.getEndTime()).getSeconds();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(stockKey, String.valueOf(stock), ttl, TimeUnit.SECONDS);
                }
            }
            remaining = redisTemplate.opsForValue().decrement(stockKey);
            if (remaining == null || remaining < 0) {
                redisTemplate.opsForValue().increment(stockKey);
                throw new BusinessException("商品已售罄");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("seckill stock deduction failed. promotionId={}, stockKey={}", promotionId, stockKey, e);
            throw new BusinessException("秒杀系统繁忙，请稍后再试");
        }

        try {
            // Deduct DB stock
            if (product.getStock() < quantity) {
                throw new BusinessException("库存不足");
            }
            product.setStock(product.getStock() - quantity);
            product.setSales(product.getSales() + quantity);
            productMapper.updateById(product);

            String orderNo = IdUtil.getSnowflakeNextIdStr();
            BigDecimal seckillPrice = promo.getDiscountValue() != null ? promo.getDiscountValue() : product.getPrice();
            BigDecimal totalAmount = seckillPrice.multiply(BigDecimal.valueOf(quantity));

            Order order = Order.builder()
                    .orderNo(orderNo)
                    .userId(userId)
                    .addressId(addressId)
                    .totalAmount(totalAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .payAmount(totalAmount)
                    .payMethod(payMethod != null ? payMethod : "ALIPAY")
                    .status("PENDING_PAY")
                    .build();
            orderMapper.insert(order);

            OrderItem orderItem = OrderItem.builder()
                    .orderNo(orderNo)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getCoverImage())
                    .price(seckillPrice)
                    .spec(spec)
                    .quantity(quantity)
                    .subtotal(totalAmount)
                    .build();
            orderItemMapper.insert(orderItem);

            OrderVO vo = OrderVO.builder()
                    .orderNo(orderNo)
                    .totalAmount(totalAmount)
                    .payAmount(totalAmount)
                    .payMethod(payMethod != null ? payMethod : "ALIPAY")
                    .status("PENDING_PAY")
                    .createTime(order.getCreateTime())
                    .items(Collections.singletonList(OrderItemVO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .productImage(product.getCoverImage())
                            .price(seckillPrice)
                            .spec(spec)
                            .quantity(quantity)
                            .subtotal(totalAmount)
                            .build()))
                    .build();

            return vo;
        } catch (Exception e) {
            redisTemplate.opsForValue().increment(stockKey);
            throw e;
        }
    }
}