package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.CartDTO;
import com.ptithcm.smartshop.dto.CheckoutFormDTO;
import com.ptithcm.smartshop.entity.ShopOrder;

import java.util.Optional;

public interface OrderService {
    ShopOrder createOrder(CheckoutFormDTO checkoutForm, CartDTO cart);
    Optional<ShopOrder> findById(String id);
}