package com.parking.smart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.smart.common.PageResult;
import com.parking.smart.dto.ReviewReplyRequest;
import com.parking.smart.dto.ReviewRequest;
import com.parking.smart.entity.Review;

public interface ReviewService extends IService<Review> {

    Review createReview(ReviewRequest request);

    PageResult<Review> getMyReviews(Integer page, Integer size);

    PageResult<Review> getAllReviews(Integer page, Integer size, Integer status);

    void replyReview(Long id, ReviewReplyRequest request);

    void updateReviewStatus(Long id, Integer status);
}
