package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.entity.Promotion;
import com.ecommerce.entity.UserCoupon;
import com.ecommerce.mapper.PromotionMapper;
import com.ecommerce.mapper.UserCouponMapper;
import com.ecommerce.product.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements PromotionService {

    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Promotion> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return this.list(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getType, "COUPON")
                .eq(Promotion::getStatus, 1)
                .le(Promotion::getStartTime, now)
                .ge(Promotion::getEndTime, now)
                .orderByDesc(Promotion::getCreateTime));
    }

    @Override
    @Transactional
    public void claimCoupon(Long userId, Long promotionId) {
        Promotion promo = this.getById(promotionId);
        if (promo == null || promo.getStatus() != 1 || !"COUPON".equals(promo.getType())) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promo.getStartTime()) || now.isAfter(promo.getEndTime())) {
            throw new BusinessException("优惠券不在有效期内");
        }
        long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getPromotionId, promotionId));
        if (count > 0) {
            throw new BusinessException(400, "您已经领取过该优惠券啦，不能贪心哦~");
        }
        UserCoupon uc = UserCoupon.builder()
                .userId(userId)
                .promotionId(promotionId)
                .status(0)
                .claimTime(now)
                .build();
        userCouponMapper.insert(uc);
    }

    @Override
    public List<Promotion> getUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0));
        if (userCoupons.isEmpty()) return Collections.emptyList();
        List<Long> promoIds = userCoupons.stream()
                .map(UserCoupon::getPromotionId).collect(Collectors.toList());
        return this.listByIds(promoIds);
    }
}