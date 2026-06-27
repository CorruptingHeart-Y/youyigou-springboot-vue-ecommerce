package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.Result;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.FeedbackRequest;
import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.ProductVO;
import com.ecommerce.entity.Feedback;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.UserFavorite;
import com.ecommerce.mapper.FeedbackMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserFavoriteMapper;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "客户端-收藏/评价/反馈")
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class UserActionController {

    private final UserFavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;
    private final FeedbackMapper feedbackMapper;

    @Operation(summary = "收藏商品")
    @PostMapping("/favorite/{productId}")
    public Result<Void> favorite(@CurrentUser LoginUser user, @PathVariable Long productId) {
        if (!favoriteMapper.exists(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, user.getUserId())
                .eq(UserFavorite::getProductId, productId))) {
            Product product = productMapper.selectById(productId);
            if (product == null) throw new BusinessException("商品不存在");
            UserFavorite fav = UserFavorite.builder().userId(user.getUserId()).productId(productId).build();
            favoriteMapper.insert(fav);
        }
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite/{productId}")
    public Result<Void> unfavorite(@CurrentUser LoginUser user, @PathVariable Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, user.getUserId())
                .eq(UserFavorite::getProductId, productId));
        return Result.success();
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/favorite/list")
    public Result<List<ProductVO>> favorites(@CurrentUser LoginUser user) {
        List<UserFavorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, user.getUserId())
                .orderByDesc(UserFavorite::getCreateTime));
        if (favorites.isEmpty()) return Result.success(List.of());
        Set<Long> productIds = favorites.stream().map(UserFavorite::getProductId).collect(Collectors.toSet());
        List<Product> products = productMapper.selectBatchIds(productIds);
        List<ProductVO> vos = products.stream().map(p -> ProductVO.builder()
                .id(p.getId())
                .name(p.getName())
                .categoryId(p.getCategoryId())
                .coverImage(p.getCoverImage())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .stock(p.getStock())
                .sales(p.getSales())
                .description(p.getDescription())
                .status(p.getStatus())
                .isFavorited(true)
                .build()).collect(Collectors.toList());
        return Result.success(vos);
    }

    @Operation(summary = "提交评价")
    @PostMapping("/review")
    public Result<Void> review(@CurrentUser LoginUser user, @Valid @RequestBody ReviewRequest request) {
        Review review = Review.builder()
                .userId(user.getUserId())
                .productId(request.getProductId())
                .content(request.getContent())
                .rating(request.getRating() != null ? request.getRating() : 5)
                .images(request.getImages())
                .status(1)
                .build();
        reviewMapper.insert(review);
        return Result.success();
    }

    @Operation(summary = "提交反馈")
    @PostMapping("/feedback")
    public Result<Void> feedback(@CurrentUser LoginUser user, @Valid @RequestBody FeedbackRequest request) {
        Feedback feedback = Feedback.builder()
                .userId(user.getUserId())
                .content(request.getContent())
                .images(request.getImages())
                .status(0)
                .build();
        feedbackMapper.insert(feedback);
        return Result.success();
    }
}