package com.ecommerce.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.common.PageResult;
import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.entity.Review;

import com.ecommerce.dto.response.ReviewVO;

public interface ReviewService extends IService<Review> {

    Page<Review> listReviews(int page, int size, Long productId, Integer status);

    Review addReview(ReviewRequest request, Long userId);

    void deleteReview(Long reviewId, Long userId);

    void updateReviewStatus(Long reviewId, Integer status);

    PageResult<ReviewVO> getProductReviews(Long productId, int page, int size);
}