package com.ptithcm.smartshop.review.repository;

import com.ptithcm.smartshop.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    Page<ProductReview> findByProductId(UUID productId, Pageable pageable);
    boolean existsByProductIdAndUserIdAndOrderId(UUID productId, UUID userId, UUID orderId);
    long countByProductId(UUID productId);
}
