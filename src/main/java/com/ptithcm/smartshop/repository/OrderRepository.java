package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findBySessionIdOrderByCreatedAtDesc(String sessionId);
}
