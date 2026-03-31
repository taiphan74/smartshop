package com.ptithcm.smartshop.controller;

import com.ptithcm.smartshop.dto.ProductDTO;
import com.ptithcm.smartshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductWebController {

    private final ProductService productService;

    @Autowired
    public ProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/san-pham/{slug}")
    public String getProductDetail(@PathVariable("slug") String slug, Model model) {
        Optional<ProductDTO> productOpt = productService.findBySlug(slug);
        
        if (productOpt.isPresent()) {
            ProductDTO productDTO = productOpt.get();
            
            // Tìm ảnh chính (Main Image)
            String mainImageUrl = "https://via.placeholder.com/400"; // Ảnh mặc định nếu chưa có
            if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
                mainImageUrl = productDTO.getImages().stream()
                        .filter(img -> img.getIsMain() != null && img.getIsMain())
                        .findFirst()
                        .map(img -> img.getImageUrl())
                        .orElse(productDTO.getImages().get(0).getImageUrl());
            }

            // Map dữ liệu để phù hợp với hiển thị trên Thymeleaf
            Map<String, Object> productView = new HashMap<>();
            productView.put("id", productDTO.getId());
            productView.put("name", productDTO.getName());
            productView.put("slug", productDTO.getSlug());
            productView.put("price", productDTO.getPrice() != null ? productDTO.getPrice() : BigDecimal.ZERO);
            productView.put("images", productDTO.getImages());
            productView.put("mainImageUrl", mainImageUrl);
            
            // Các dữ liệu hiển thị (mock data nếu Entity chưa có)
            productView.put("originalPrice", productDTO.getPrice() != null ? productDTO.getPrice().multiply(new BigDecimal("1.3")) : new BigDecimal("770000"));
            productView.put("discountPercent", 30);
            productView.put("rating", 4.9);
            productView.put("reviewCount", "28,6k");


            model.addAttribute("product", productView);
            return "product-detail";
        }
        
        // Nếu không tìm thấy, có thể trả về một trang 404 hoặc chuyển về trang chủ
        return "redirect:/";
    }
}
