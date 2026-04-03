package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.CartDTO;
import com.ptithcm.smartshop.dto.CartItemDTO;
import com.ptithcm.smartshop.dto.CheckoutFormDTO;
import com.ptithcm.smartshop.entity.ShopOrder;
import com.ptithcm.smartshop.entity.ShopOrderItem;
import com.ptithcm.smartshop.repository.ShopOrderRepository;
import com.ptithcm.smartshop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final ShopOrderRepository shopOrderRepository;

    public OrderServiceImpl(ShopOrderRepository shopOrderRepository) {
        this.shopOrderRepository = shopOrderRepository;
    }

    @Override
    public ShopOrder createOrder(CheckoutFormDTO checkoutForm, CartDTO cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        ShopOrder order = new ShopOrder();
        order.setOrderCode(generateOrderCode());
        order.setCustomerName(checkoutForm.getFullName());
        order.setPhone(checkoutForm.getPhone());
        order.setAddress(checkoutForm.getAddress());
        order.setNote(checkoutForm.getNote());
        order.setStatus("PENDING");
        order.setTotalQuantity(cart.getTotalQuantity());
        order.setTotalAmount(cart.getTotalAmount());

        for (CartItemDTO cartItem : cart.getItems()) {
            ShopOrderItem item = new ShopOrderItem();
            item.setProductId(cartItem.getProductId());
            item.setProductSlug(cartItem.getSlug());
            item.setProductName(cartItem.getName());
            item.setImageUrl(cartItem.getImageUrl());
            item.setUnitPrice(cartItem.getPrice() != null ? cartItem.getPrice() : BigDecimal.ZERO);
            item.setQuantity(cartItem.getQuantity());
            item.setSubtotal(cartItem.getSubtotal());

            order.addItem(item);
        }

        return shopOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShopOrder> findById(String id) {
        return shopOrderRepository.findById(id);
    }

    private String generateOrderCode() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "DH" + timePart + randomPart;
    }
}