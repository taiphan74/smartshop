package com.ptithcm.smartshop.review.controller;

import com.ptithcm.smartshop.review.dto.ReviewRequest;
import com.ptithcm.smartshop.review.dto.ReviewResponse;
import com.ptithcm.smartshop.review.service.ProductReviewService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ProductReviewRestController {

    private final ProductReviewService reviewService;

    public ProductReviewRestController(ProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(productId, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @GetMapping("/eligible-orders")
    public ResponseEntity<List<UUID>> getEligibleOrders(
            @PathVariable UUID productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reviewService.getEligibleOrdersForReview(productId, userDetails.getId()));
    }
}
