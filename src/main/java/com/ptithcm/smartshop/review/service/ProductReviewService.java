package com.ptithcm.smartshop.review.service;

import com.ptithcm.smartshop.review.dto.ReviewRequest;
import com.ptithcm.smartshop.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProductReviewService {
    ReviewResponse createReview(UUID productId, UUID userId, ReviewRequest request);
    Page<ReviewResponse> getProductReviews(UUID productId, int page, int size);
    List<UUID> getEligibleOrdersForReview(UUID productId, UUID userId);
}
