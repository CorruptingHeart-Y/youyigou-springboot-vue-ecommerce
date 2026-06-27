package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.dto.request.FeedbackRequest;
import com.ecommerce.entity.Feedback;

public interface FeedbackService extends IService<Feedback> {

    void submitFeedback(Long userId, FeedbackRequest request);

    Page<Feedback> listFeedbacks(int page, int size, Integer status);

    void replyFeedback(Long feedbackId, Long adminId, String reply);

    void markFeedbackResolved(Long feedbackId);
}