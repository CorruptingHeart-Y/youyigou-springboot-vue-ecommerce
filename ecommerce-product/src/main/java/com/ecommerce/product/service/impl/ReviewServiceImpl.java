package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.ReviewVO;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.common.PageResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Review> listReviews(int page, int size, Long productId, Integer status) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(Review::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public Review addReview(ReviewRequest request, Long userId) {
        long count = orderItemMapper.selectCount(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getProductId, request.getProductId())
                .inSql(OrderItem::getOrderNo, "SELECT order_no FROM e_order WHERE user_id = " + userId));
        if (count == 0) {
            throw new BusinessException("only purchased users can review");
        }
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setStatus(1);
        this.save(review);
        return review;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException("review not found");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("not your review");
        }
        this.removeById(reviewId);
    }

    @Override
    @Transactional
    public void updateReviewStatus(Long reviewId, Integer status) {
        Review review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException("review not found");
        }
        review.setStatus(status);
        this.updateById(review);
    }

    @Override
    public PageResult<ReviewVO> getProductReviews(Long productId, int page, int size) {
        Page<Review> reviewPage = this.page(new Page<>(page, size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreateTime));
        List<ReviewVO> records = reviewPage.getRecords().stream().map(r -> {
            ReviewVO vo = ReviewVO.builder()
                    .id(r.getId())
                    .userId(r.getUserId())
                    .productId(r.getProductId())
                    .content(r.getContent())
                    .rating(r.getRating())
                    .images(r.getImages())
                    .status(r.getStatus())
                    .createTime(r.getCreateTime())
                    .build();
            User u = userMapper.selectById(r.getUserId());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            return vo;
        }).toList();
        return PageResult.of(reviewPage.getTotal(), page, size, records);
    }
}