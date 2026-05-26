package com.ptithcm.smartshop.order.domain.repository;

import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

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

    List<Order> findByOrderByCreatedAtDesc(Pageable pageable);
    long countByShopId(UUID shopId);

    @Query("select coalesce(sum(o.finalAmount), 0) from Order o where o.shop.id = :shopId")
    BigDecimal sumFinalAmountByShopId(@Param("shopId") UUID shopId);
}
