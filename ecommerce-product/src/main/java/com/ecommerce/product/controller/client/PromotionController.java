package com.ecommerce.product.controller.client;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Promotion;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.PromotionMapper;
import com.ecommerce.product.service.PromotionService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Client - Promotion")
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionMapper promotionMapper;
    private final PromotionService promotionService;
    private final ProductMapper productMapper;

    @Operation(summary = "Active promotions")
    @GetMapping
    public Result<List<Promotion>> active() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getStatus, 1)
                        .le(Promotion::getStartTime, now)
                        .ge(Promotion::getEndTime, now)
                        .orderByDesc(Promotion::getCreateTime));

        // 为秒杀活动填充关联商品的封面图
        List<Long> productIds = promotions.stream()
                .filter(p -> "SECKILL".equals(p.getType()) && p.getProductId() != null)
                .map(Promotion::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
            promotions.stream()
                    .filter(p -> "SECKILL".equals(p.getType()) && p.getProductId() != null)
                    .forEach(p -> {
                        Product prod = productMap.get(p.getProductId());
                        if (prod != null) {
                            p.setCoverImage(prod.getCoverImage());
                            int sold = prod.getSales() != null ? prod.getSales() : 0;
                            int total = sold + (p.getSeckillStock() != null ? p.getSeckillStock() : (prod.getStock() != null ? prod.getStock() : 0));
                            p.setSoldCount(sold);
                            p.setTotalStock(total > 0 ? total : 100);
                        }
                    });
        }

        return Result.success(promotions);
    }

    @Operation(summary = "Promotion detail")
    @GetMapping("/{id}")
    public Result<Promotion> detail(@PathVariable Long id) {
        return Result.success(promotionMapper.selectById(id));
    }

    @Operation(summary = "Available coupons")
    @GetMapping("/coupons")
    public Result<List<Promotion>> coupons() {
        return Result.success(promotionService.getAvailableCoupons());
    }

    @Operation(summary = "Claim coupon")
    @PostMapping("/coupon/{id}/claim")
    public Result<Void> claimCoupon(@PathVariable Long id, @CurrentUser LoginUser user) {
        if (user == null) {
            return Result.unauthorized("请先登录");
        }
        promotionService.claimCoupon(user.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "My coupons")
    @GetMapping("/my-coupons")
    public Result<List<Promotion>> myCoupons(@CurrentUser LoginUser user) {
        if (user == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(promotionService.getUserCoupons(user.getUserId()));
    }
}