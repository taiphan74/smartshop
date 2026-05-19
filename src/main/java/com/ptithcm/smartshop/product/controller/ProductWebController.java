package com.ptithcm.smartshop.product.controller;

import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductWebController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductWebController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getHome(@RequestParam(value = "category", required = false) String categorySlug, Model model) {
        if (categorySlug == null || categorySlug.isBlank()) {
            List<ProductListDTO> products = productService.findAllProducts();
            model.addAttribute("products", products);
            model.addAttribute("activeCategory", null);
            model.addAttribute("childCategories", List.of());
            model.addAttribute("selectedCategorySlug", null);
            return "product/home";
        }

        Optional<CategoryDTO> categoryOpt = categoryService.findBySlug(categorySlug);
        if (categoryOpt.isEmpty()) {
            return "error/404";
        }

        CategoryDTO activeCategory = categoryOpt.get();
        List<ProductListDTO> products = productService.findPublicProductsByCategorySlug(categorySlug);
        List<CategoryDTO> childCategories = categoryService.findChildrenBySlug(categorySlug);

        model.addAttribute("products", products);
        model.addAttribute("activeCategory", activeCategory);
        model.addAttribute("childCategories", childCategories);
        model.addAttribute("selectedCategorySlug", categorySlug);
        return "product/home";
    }

    @GetMapping("/{slug}")
    public String getProductDetail(@PathVariable("slug") String slug, Model model) {
        Optional<ProductDetailDTO> productOpt = productService.findBySlug(slug);

        if (productOpt.isPresent()) {
            ProductDetailDTO productDTO = productOpt.get();

            String mainImageUrl = "https://via.placeholder.com/400";
            if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
                mainImageUrl = productDTO.getImages().stream()
                        .filter(img -> img.getIsMain() != null && img.getIsMain())
                        .findFirst()
                        .map(img -> img.getImageUrl())
                        .orElse(productDTO.getImages().get(0).getImageUrl());
            }

            Map<String, Object> productView = new HashMap<>();
            productView.put("id", productDTO.getId());
            productView.put("name", productDTO.getName());
            productView.put("slug", productDTO.getSlug());
            productView.put("price", productDTO.getPrice() != null ? productDTO.getPrice() : BigDecimal.ZERO);
            productView.put("images", productDTO.getImages());
            productView.put("mainImageUrl", mainImageUrl);
            productView.put("description", productDTO.getDescription());
            productView.put("stockQuantity", productDTO.getStockQuantity());
            productView.put("categoryName", productDTO.getCategoryName());
            productView.put("reviewCount", productDTO.getReviewCount());
            productView.put("ratingSum", productDTO.getRatingSum());
            productView.put("averageRating", productDTO.getAverageRating());

            model.addAttribute("product", productView);
            return "product/detail";
        }

        return "error/404";
    }
}
