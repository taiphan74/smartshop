package com.ptithcm.smartshop.order.repository;

import com.ptithcm.smartshop.order.entity.SessionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionOrderItemRepository extends JpaRepository<SessionOrderItem, String> {
}