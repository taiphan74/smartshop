package com.ptithcm.smartshop.controller;

import com.ptithcm.smartshop.dto.CartDTO;
import com.ptithcm.smartshop.dto.CartItemDTO;
import com.ptithcm.smartshop.dto.CheckoutFormDTO;
import com.ptithcm.smartshop.entity.ShopOrder;
import com.ptithcm.smartshop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(
            @SessionAttribute(name = "cart", required = false) CartDTO cart,
            Model model
    ) {
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        if (!model.containsAttribute("checkoutForm")) {
            model.addAttribute("checkoutForm", new CheckoutFormDTO());
        }

        model.addAttribute("cart", cart);
        return "checkout/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @ModelAttribute("checkoutForm") CheckoutFormDTO checkoutForm,
            @SessionAttribute(name = "cart", required = false) CartDTO cart,
            Model model
    ) {
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        if (isBlank(checkoutForm.getFullName())
                || isBlank(checkoutForm.getPhone())
                || isBlank(checkoutForm.getAddress())) {
            model.addAttribute("cart", cart);
            model.addAttribute("error", "Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ.");
            return "checkout/checkout";
        }

        ShopOrder savedOrder = orderService.createOrder(checkoutForm, cart);

        cart.clear();

        return "redirect:/checkout/success/" + savedOrder.getId();
    }

    @GetMapping("/checkout/success/{id}")
    public String successPage(@PathVariable("id") String id, Model model) {
        ShopOrder order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        model.addAttribute("order", order);
        return "checkout/success";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
