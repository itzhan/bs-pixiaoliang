package com.parking.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.smart.common.BusinessException;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ReviewReplyRequest;
import com.parking.smart.dto.ReviewRequest;
import com.parking.smart.entity.Review;
import com.parking.smart.mapper.ReviewMapper;
import com.parking.smart.service.ReviewService;
import com.parking.smart.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Override
    public Review createReview(ReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查该订单是否已被评价
        if (request.getOrderId() != null) {
            LambdaQueryWrapper<Review> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(Review::getUserId, userId)
                    .eq(Review::getOrderId, request.getOrderId());
            long count = count(existWrapper);
            if (count > 0) {
                throw new BusinessException("该订单已评价，不能重复评价");
            }
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        save(review);
        log.info("评价创建成功, id={}, userId={}, orderId={}", review.getId(), userId, request.getOrderId());
        return review;
    }

    @Override
    public PageResult<Review> getMyReviews(Integer page, Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreatedAt);
        IPage<Review> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public PageResult<Review> getAllReviews(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreatedAt);
        IPage<Review> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.from(iPage);
    }

    @Override
    public void replyReview(Long id, ReviewReplyRequest request) {
        Review review = getById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setReply(request.getReply());
        review.setReplyAt(LocalDateTime.now());
        updateById(review);
        log.info("评价回复成功, id={}", id);
    }

    @Override
    public void updateReviewStatus(Long id, Integer status) {
        Review review = getById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setStatus(status);
        updateById(review);
        log.info("评价状态更新成功, id={}, status={}", id, status);
    }
}
