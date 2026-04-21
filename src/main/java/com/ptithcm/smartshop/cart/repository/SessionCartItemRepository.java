package com.ptithcm.smartshop.cart.repository;

import com.ptithcm.smartshop.cart.entity.SessionCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionCartItemRepository extends JpaRepository<SessionCartItem, String> {
}