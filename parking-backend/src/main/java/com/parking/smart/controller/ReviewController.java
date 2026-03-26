package com.parking.smart.controller;

import com.parking.smart.common.PageResult;
import com.parking.smart.common.Result;
import com.parking.smart.dto.ReviewReplyRequest;
import com.parking.smart.dto.ReviewRequest;
import com.parking.smart.entity.Review;
import com.parking.smart.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping
    public Result<Review> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("创建评价");
        Review review = reviewService.createReview(request);
        return Result.success(review);
    }

    /**
     * 查询我的评价
     */
    @GetMapping("/my")
    public Result<PageResult<Review>> getMyReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Review> result = reviewService.getMyReviews(page, size);
        return Result.success(result);
    }

    /**
     * 查询所有评价（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<PageResult<Review>> getAllReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        PageResult<Review> result = reviewService.getAllReviews(page, size, status);
        return Result.success(result);
    }

    /**
     * 回复评价（管理员）
     */
    @PutMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public Result<Void> replyReview(@PathVariable Long id,
                                    @Valid @RequestBody ReviewReplyRequest request) {
        log.info("回复评价: id={}", id);
        reviewService.replyReview(id, request);
        return Result.success();
    }

    /**
     * 更新评价状态（管理员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateReviewStatus(@PathVariable Long id,
                                           @RequestParam Integer status) {
        log.info("更新评价状态: id={}, status={}", id, status);
        reviewService.updateReviewStatus(id, status);
        return Result.success();
    }

    /**
     * 删除评价（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteReview(@PathVariable Long id) {
        log.info("管理员删除评价: id={}", id);
        reviewService.removeById(id);
        return Result.success();
    }
}
