package com.ptithcm.smartshop.controller;

import com.ptithcm.smartshop.dto.CartDTO;
import com.ptithcm.smartshop.dto.CartItemDTO;
import com.ptithcm.smartshop.dto.ProductDetailDTO;
import com.ptithcm.smartshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@SessionAttributes("cart")
public class CartController {

    private final ProductService productService;

    @Autowired
    public CartController(ProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute("cart")
    public CartDTO cart() {
        return new CartDTO();
    }

    @GetMapping("/cart")
    public String viewCart(@ModelAttribute("cart") CartDTO cart, Model model) {
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam("slug") String slug,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @ModelAttribute("cart") CartDTO cart
    ) {
        int finalQuantity = Math.max(1, quantity);
        Optional<ProductDetailDTO> productOpt = productService.findBySlug(slug);

        if (productOpt.isPresent()) {
            ProductDetailDTO product = productOpt.get();
            CartItemDTO item = new CartItemDTO(
                    product.getId(),
                    product.getSlug(),
                    product.getName(),
                    product.getImages() != null && !product.getImages().isEmpty()
                            ? product.getImages().get(0).getImageUrl() : "https://via.placeholder.com/100",
                    product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO,
                    finalQuantity
            );
            cart.addItem(item);
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(
            @RequestParam("slug") String slug,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @ModelAttribute("cart") CartDTO cart
    ) {
        int finalQuantity = Math.max(1, quantity);
        cart.updateItemQuantity(slug, finalQuantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(
            @RequestParam("slug") String slug,
            @ModelAttribute("cart") CartDTO cart
    ) {
        cart.removeItem(slug);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(@ModelAttribute("cart") CartDTO cart) {
        cart.clear();
        return "redirect:/cart";
    }
}
