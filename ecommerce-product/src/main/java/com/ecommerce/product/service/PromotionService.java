package com.ecommerce.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.entity.Promotion;

import java.util.List;

public interface PromotionService extends IService<Promotion> {

    List<Promotion> getAvailableCoupons();

    void claimCoupon(Long userId, Long promotionId);

    List<Promotion> getUserCoupons(Long userId);
}