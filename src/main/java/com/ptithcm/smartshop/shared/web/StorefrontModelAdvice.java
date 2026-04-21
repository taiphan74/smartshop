package com.ptithcm.smartshop.shared.web;

import com.ptithcm.smartshop.cart.service.CartService;
import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {
    "com.ptithcm.smartshop.auth.controller",
    "com.ptithcm.smartshop.product.controller",
    "com.ptithcm.smartshop.cart.controller",
    "com.ptithcm.smartshop.order.controller"
})
public class StorefrontModelAdvice {

    private final CategoryService categoryService;
    private final CartService cartService;

    public StorefrontModelAdvice(CategoryService categoryService, CartService cartService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @ModelAttribute("headerCategories")
    public List<CategoryDTO> headerCategories() {
        PageResponse<CategoryDTO> categoryPage = categoryService.findParentCategories(0, 8, "name", "asc");
        return categoryPage.getContent();
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request) {
        return cartService.getCartCount(request.getSession());
    }

    @ModelAttribute("sessionUser")
    public SessionUser sessionUser(HttpServletRequest request) {
        Object sessionUser = request.getSession(false) != null
                ? request.getSession(false).getAttribute(SessionConstants.CURRENT_USER)
                : null;
        return sessionUser instanceof SessionUser user ? user : null;
    }
}