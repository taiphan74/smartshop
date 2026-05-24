package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BannerService bannerService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(BannerService bannerService,
                          ProductService productService,
                          CategoryService categoryService) {
        this.bannerService = bannerService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.CURRENT_USER);
            if (sessionUser != null) {
                model.addAttribute("sessionUser", sessionUser);
            }
        }

        PageResponse<CategoryDTO> categoryPage = categoryService.findParentCategories(0, 50, "name", "asc");
        List<CategoryDTO> rootCategories = categoryPage.getContent();
        model.addAttribute("rootCategories", rootCategories);

        model.addAttribute("headerCategories", rootCategories.stream().limit(8).toList());

        List<ProductListDTO> products = productService.findAllProducts();
        model.addAttribute("products", products);

        model.addAttribute("banners", bannerService.findAllActive());
        return "home";
    }
}
