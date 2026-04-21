package com.ptithcm.smartshop.order.repository;

import com.ptithcm.smartshop.order.entity.SessionOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionOrderRepository extends JpaRepository<SessionOrder, String> {

    List<SessionOrder> findBySessionIdOrderByCreatedAtDesc(String sessionId);
}