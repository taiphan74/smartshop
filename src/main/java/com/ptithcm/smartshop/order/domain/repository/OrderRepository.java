package com.ptithcm.smartshop.order.domain.repository;

import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Override
    @EntityGraph(attributePaths = {"shop", "user", "items"})
    Optional<Order> findById(UUID id);

    @Query("""
            select distinct o from Order o
            join o.items i
            where o.user.id = :userId
              and o.status = :status
              and i.product.id = :productId
            """)
    List<Order> findDeliveredOrdersContainingProduct(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId,
            @Param("status") OrderStatus status);

    @Query("""
            select coalesce(sum(o.finalAmount), 0)
            from Order o
            where o.status in :statuses
            """)
    BigDecimal sumFinalAmountByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    long countByCreatedAtBetween(Instant startInclusive, Instant endExclusive);

    long countByCreatedAtBetweenAndStatusIn(
            Instant startInclusive,
            Instant endExclusive,
            List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"shop", "user", "items"})
    Page<Order> findByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "user", "items"})
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByShopId(UUID shopId);
}

