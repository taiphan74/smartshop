package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.cart.CartDTO;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    
    CartDTO getCart(HttpSession session);
    
    void addToCart(HttpSession session, String productId, String variantId, Integer quantity);
    
    void updateQuantity(HttpSession session, String productId, String variantId, Integer quantity);
    
    void removeItem(HttpSession session, String productId, String variantId);
    
    void clearCart(HttpSession session);
}
