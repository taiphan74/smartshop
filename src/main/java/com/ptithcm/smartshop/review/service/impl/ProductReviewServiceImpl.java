package com.ptithcm.smartshop.review.service.impl;

import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.review.dto.ReviewRequest;
import com.ptithcm.smartshop.review.dto.ReviewResponse;
import com.ptithcm.smartshop.review.entity.ProductReview;
import com.ptithcm.smartshop.review.repository.ProductReviewRepository;
import com.ptithcm.smartshop.review.service.ProductReviewService;
import com.ptithcm.smartshop.shared.exception.BadRequestException;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ProductReviewServiceImpl(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(UUID productId, UUID userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Đơn hàng không thuộc về bạn");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ được đánh giá đơn hàng đã giao thành công");
        }

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!productInOrder) {
            throw new ResourceNotFoundException("Sản phẩm không có trong đơn hàng này");
        }

        if (reviewRepository.existsByProductIdAndUserIdAndOrderId(productId, userId, request.getOrderId())) {
            throw new ConflictException("Bạn đã đánh giá sản phẩm này cho đơn hàng trên");
        }

        User user = userRepository.getReferenceById(userId);

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        ProductReview savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(UUID productId, int page, int size) {
        return reviewRepository.findByProductId(productId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    public List<UUID> getEligibleOrdersForReview(UUID productId, UUID userId) {
        return orderRepository.findDeliveredOrdersContainingProduct(userId, productId, OrderStatus.DELIVERED)
                .stream()
                .filter(o -> !reviewRepository.existsByProductIdAndUserIdAndOrderId(productId, userId, o.getId()))
                .map(Order::getId)
                .toList();
    }

    private ReviewResponse mapToResponse(ProductReview review) {
        ReviewResponse res = new ReviewResponse();
        res.setId(review.getId());
        res.setRating(review.getRating());
        res.setComment(review.getComment());
        res.setCreatedAt(review.getCreatedAt());
        res.setOrderId(review.getOrder().getId());

        User user = review.getUser();
        res.setUserName(user.getProfile() != null ? user.getProfile().getFullName() : user.getEmail());
        return res;
    }
}
