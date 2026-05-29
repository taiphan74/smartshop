package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.review.entity.ProductReview;
import com.ptithcm.smartshop.review.repository.ProductReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReviewManagementService {

    private final ProductReviewRepository reviewRepository;

    public AdminReviewManagementService(ProductReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductReview> list(Boolean visible, Pageable pageable) {
        if (visible == null) {
            return reviewRepository.findAll(pageable);
        }
        return reviewRepository.findByVisible(visible, pageable);
    }

    @Transactional(readOnly = true)
    public ProductReview get(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));
    }

    @Transactional
    public void hide(UUID reviewId) {
        ProductReview review = get(reviewId);
        review.setVisible(false);
        recalculateProductSummary(review.getProduct());
    }

    @Transactional
    public void show(UUID reviewId) {
        ProductReview review = get(reviewId);
        review.setVisible(true);
        recalculateProductSummary(review.getProduct());
    }

    @Transactional
    public void delete(UUID reviewId) {
        ProductReview review = get(reviewId);
        Product product = review.getProduct();
        reviewRepository.delete(review);
        reviewRepository.flush();
        recalculateProductSummary(product);
    }

    private void recalculateProductSummary(Product product) {
        List<ProductReview> visibleReviews = reviewRepository.findByProductIdAndVisible(product.getId(), true);
        long reviewCount = visibleReviews.size();
        long ratingSum = visibleReviews.stream()
                .mapToLong(ProductReview::getRating)
                .sum();
        BigDecimal averageRating = reviewCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ratingSum).divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);

        product.setReviewCount(reviewCount);
        product.setRatingSum(ratingSum);
        product.setAverageRating(averageRating);
    }
}
