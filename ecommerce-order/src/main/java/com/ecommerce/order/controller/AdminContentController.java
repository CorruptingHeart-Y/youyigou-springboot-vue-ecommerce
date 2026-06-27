package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.response.ReviewVO;
import com.ecommerce.entity.Feedback;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.order.service.FeedbackService;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理后台-内容管理")
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('content:manage')")
public class AdminContentController {

    private final ReviewMapper reviewMapper;
    private final FeedbackService feedbackService;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Operation(summary = "评价列表")
    @GetMapping("/reviews")
    public Result<PageResult<ReviewVO>> reviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .orderByDesc(Review::getCreateTime);
        Page<Review> pages = reviewMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReviewVO> records = pages.getRecords().stream().map(r -> {
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
            if (u != null) vo.setUsername(u.getUsername());
            Product p = productMapper.selectById(r.getProductId());
            if (p != null) vo.setProductName(p.getName());
            return vo;
        }).toList();
        return Result.success(PageResult.of(pages.getTotal(), pages.getCurrent(), pages.getSize(), records));
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/review/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(reviewId);
        return Result.success();
    }

    @Operation(summary = "反馈列表")
    @GetMapping("/feedbacks")
    public Result<PageResult<Feedback>> feedbacks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        var pages = feedbackService.listFeedbacks(page, size, status);
        return Result.success(PageResult.of(pages.getTotal(), pages.getCurrent(), pages.getSize(), pages.getRecords()));
    }

    @Operation(summary = "回复反馈")
    @PutMapping("/feedback/{feedbackId}/reply")
    public Result<Void> replyFeedback(@CurrentUser LoginUser user,
                                       @PathVariable Long feedbackId,
                                       @RequestBody Map<String, String> params) {
        feedbackService.replyFeedback(feedbackId, user.getUserId(), params.get("reply"));
        return Result.success();
    }

    @Operation(summary = "标记反馈已处理")
    @PutMapping("/feedback/{feedbackId}/resolve")
    public Result<Void> resolveFeedback(@PathVariable Long feedbackId) {
        feedbackService.markFeedbackResolved(feedbackId);
        return Result.success();
    }
}