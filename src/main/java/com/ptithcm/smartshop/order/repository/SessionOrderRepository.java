package com.ptithcm.smartshop.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ptithcm.smartshop.order.entity.SessionOrder;

@Repository
public interface SessionOrderRepository extends JpaRepository<SessionOrder, String> {

    List<SessionOrder> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    List<SessionOrder> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    @EntityGraph(attributePaths = "items")
    Optional<SessionOrder> findByOrderCodeAndCustomerPhone(String orderCode, String customerPhone);

    @EntityGraph(attributePaths = "items")
    Optional<SessionOrder> findByOrderCode(String orderCode);
}