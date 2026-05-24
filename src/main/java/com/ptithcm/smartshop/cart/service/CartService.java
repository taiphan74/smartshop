package com.ptithcm.smartshop.cart.service;

import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;

import jakarta.servlet.http.HttpSession;

public interface CartService {

    CartDTO getCart(HttpSession session);

    int getCartCount(HttpSession session);

    void addToCart(HttpSession session, String productId, String variantId, Integer quantity);

    void updateQuantity(HttpSession session, String productId, String variantId, Integer quantity);

    void removeItem(HttpSession session, String productId, String variantId);

    void applyVoucher(HttpSession session, String voucherCode, VoucherScope scope);

    void removeVoucher(HttpSession session, VoucherScope scope);

    void clearCart(HttpSession session);
}