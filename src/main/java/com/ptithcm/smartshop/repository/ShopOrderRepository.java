package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, String> {
}