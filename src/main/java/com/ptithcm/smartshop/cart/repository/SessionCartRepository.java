package com.ptithcm.smartshop.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ptithcm.smartshop.cart.entity.SessionCart;

@Repository
public interface SessionCartRepository extends JpaRepository<SessionCart, String> {

    Optional<SessionCart> findBySessionId(String sessionId);

    Optional<SessionCart> findByUserId(UUID userId);
}