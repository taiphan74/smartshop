package com.ptithcm.smartshop.product.controller;

import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartItemDTO;
import com.ptithcm.smartshop.product.entity.Order;
import com.ptithcm.smartshop.product.entity.OrderItem;
import com.ptithcm.smartshop.product.repository.OrderItemRepository;
import com.ptithcm.smartshop.product.repository.OrderRepository;
import com.ptithcm.smartshop.product.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/checkout")
public class CheckoutWebController {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CheckoutWebController(CartService cartService, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping
    public String viewCheckout(HttpSession session, Model model) {
        CartDTO cart = cartService.getCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        return "checkout/index";
    }

    @PostMapping("/process")
    public String processCheckout(@RequestParam String customerName,
                                  @RequestParam String customerPhone,
                                  @RequestParam String customerAddress,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        CartDTO cart = cartService.getCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setSessionId(session.getId());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        order.setShippingFee(cart.getShippingFee());
        order.setSubTotal(cart.getSubTotal());
        order.setTotalAmount(cart.getTotal());
        order.setStatus("PENDING");
        order.setPaymentMethod("COD");

        order = orderRepository.save(order);

        // Chép Items từ Cart sang Order
        for (CartItemDTO itemDto : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemDto.getProductId());
            orderItem.setVariantId(itemDto.getVariantId());
            orderItem.setProductName(itemDto.getProductName());
            orderItem.setImageUrl(itemDto.getImageUrl());
            orderItem.setPrice(itemDto.getPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItemRepository.save(orderItem);
        }

        // Xóa giỏ hàng
        cartService.clearCart(session);

        redirectAttributes.addFlashAttribute("orderCode", order.getOrderCode());
        return "redirect:/checkout/success";
    }

    @GetMapping("/success")
    public String checkoutSuccess(Model model) {
        return "checkout/success";
    }
}
