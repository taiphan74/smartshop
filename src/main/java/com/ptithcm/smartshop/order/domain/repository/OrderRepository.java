package com.ptithcm.smartshop.order.domain.repository;

import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

