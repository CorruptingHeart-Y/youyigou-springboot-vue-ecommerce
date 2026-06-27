package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.dto.request.FeedbackRequest;
import com.ecommerce.entity.Feedback;
import com.ecommerce.mapper.FeedbackMapper;
import com.ecommerce.order.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    @Transactional
    public void submitFeedback(Long userId, FeedbackRequest request) {
        Feedback feedback = Feedback.builder()
                .userId(userId)
                .content(request.getContent())
                .images(request.getImages())
                .status(0)
                .build();
        this.save(feedback);
    }

    @Override
    public Page<Feedback> listFeedbacks(int page, int size, Integer status) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreateTime);
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void replyFeedback(Long feedbackId, Long adminId, String reply) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        feedback.setReply(reply);
        feedback.setReplyBy(adminId);
        feedback.setReplyTime(LocalDateTime.now());
        this.updateById(feedback);
    }

    @Override
    @Transactional
    public void markFeedbackResolved(Long feedbackId) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        feedback.setStatus(1);
        this.updateById(feedback);
    }
}