package com.ptithcm.smartshop.order.service;

import com.ptithcm.smartshop.order.dto.request.CheckoutRequest;
import jakarta.servlet.http.HttpSession;

public interface CheckoutService {

    String placeOrder(HttpSession session, CheckoutRequest request);
}