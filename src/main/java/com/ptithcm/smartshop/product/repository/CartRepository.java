package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findBySessionId(String sessionId);
}
