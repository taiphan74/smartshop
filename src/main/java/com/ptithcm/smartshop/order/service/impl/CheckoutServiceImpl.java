package com.ptithcm.smartshop.order.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptithcm.smartshop.cart.service.CartService;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartItemDTO;
import com.ptithcm.smartshop.order.dto.request.CheckoutRequest;
import com.ptithcm.smartshop.order.entity.SessionOrder;
import com.ptithcm.smartshop.order.entity.SessionOrderItem;
import com.ptithcm.smartshop.order.repository.SessionOrderItemRepository;
import com.ptithcm.smartshop.order.repository.SessionOrderRepository;
import com.ptithcm.smartshop.order.service.CheckoutService;
import com.ptithcm.smartshop.voucher.service.VoucherService;

import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final SessionOrderRepository orderRepository;
    private final SessionOrderItemRepository orderItemRepository;
    private final VoucherService voucherService;

    public CheckoutServiceImpl(
            CartService cartService,
            SessionOrderRepository orderRepository,
            SessionOrderItemRepository orderItemRepository,
            VoucherService voucherService) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.voucherService = voucherService;
    }

    @Override
    @Transactional
    public String placeOrder(HttpSession session, CheckoutRequest request) {
        CartDTO cart = cartService.getCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        SessionOrder order = new SessionOrder();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setSessionId(session.getId());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerAddress(request.getCustomerAddress());
        order.setShippingFee(cart.getShippingFee());
        order.setSubTotal(cart.getSubTotal());
        order.setTotalAmount(cart.getTotalBeforeDiscount());
        order.setDiscountAmount(cart.getOrderDiscountAmount());
        order.setShippingDiscountAmount(cart.getShippingDiscountAmount());
        order.setFinalAmount(cart.getTotal());
        order.setOrderVoucherCode(cart.getAppliedOrderVoucherCode());
        order.setShippingVoucherCode(cart.getAppliedShippingVoucherCode());
        order.setStatus("PENDING");
        order.setPaymentMethod("COD");

        order = orderRepository.save(order);

        for (CartItemDTO itemDto : cart.getItems()) {
            SessionOrderItem orderItem = new SessionOrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemDto.getProductId());
            orderItem.setVariantId(itemDto.getVariantId());
            orderItem.setProductName(itemDto.getProductName());
            orderItem.setImageUrl(itemDto.getImageUrl());
            orderItem.setPrice(itemDto.getPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItemRepository.save(orderItem);
        }

        voucherService.recordVoucherUsage(cart);
        cartService.clearCart(session);
        return order.getOrderCode();
    }
}