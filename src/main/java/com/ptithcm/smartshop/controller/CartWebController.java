package com.ptithcm.smartshop.controller;

import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartWebController {

    private final CartService cartService;

    public CartWebController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        CartDTO cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart/index";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam(required = false) String variantId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            @RequestParam(required = false) String redirectUrl,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, variantId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng");
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam String productId,
                                 @RequestParam(required = false) String variantId,
                                 @RequestParam Integer quantity,
                                 HttpSession session) {
        cartService.updateQuantity(session, productId, variantId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam String productId,
                             @RequestParam(required = false) String variantId,
                             HttpSession session) {
        cartService.removeItem(session, productId, variantId);
        return "redirect:/cart";
    }
}
