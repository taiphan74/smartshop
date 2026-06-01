package com.ptithcm.smartshop.seller.controller;

import com.ptithcm.smartshop.product.config.ProductUploadConfig;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.util.FileUploadUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/seller/products")
public class SellerProductController extends BaseSellerController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ShopRepository shopRepository;
    private final ProductUploadConfig productUploadConfig;

    public SellerProductController(ProductService productService,
                                   CategoryService categoryService,
                                   ShopRepository shopRepository,
                                   ProductUploadConfig productUploadConfig) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.shopRepository = shopRepository;
        this.productUploadConfig = productUploadConfig;
    }

    @GetMapping
    public String list(@RequestParam UUID shopId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String direction,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        verifyShopOwnership(shopId, userDetails.getId());

        PageResponse<ProductListDTO> pageResponse = productService.findProductsByShop(shopId, page, size, sort, direction);
        model.addAttribute("products", pageResponse.getContent());
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("shopId", shopId);
        model.addAttribute("activeMenu", "products");
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        return "seller/products/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam UUID shopId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        verifyShopOwnership(shopId, userDetails.getId());

        model.addAttribute("product", null);
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.findAll(0, 100, "name", "asc").getContent());
        model.addAttribute("shopId", shopId);
        model.addAttribute("activeMenu", "products");
        return "seller/products/form";
    }

    @PostMapping
    public String create(@RequestParam UUID shopId,
                         @ModelAttribute ProductRequest request,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        verifyShopOwnership(shopId, userDetails.getId());

        request.setShopId(shopId.toString());
        request.setStatus(request.getStatus() != null ? request.getStatus() : true);
        productService.save(request);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm thành công");
        return "redirect:/seller/products?shopId=" + shopId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id,
                           @RequestParam UUID shopId,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        verifyShopOwnership(shopId, userDetails.getId());

        ProductDetailDTO product = productService.findById(id.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setStatus(product.getStatus());
        request.setCategoryId(product.getCategoryId());
        request.setShopId(shopId.toString());

        model.addAttribute("product", product);
        model.addAttribute("productRequest", request);
        model.addAttribute("categories", categoryService.findAll(0, 100, "name", "asc").getContent());
        model.addAttribute("shopId", shopId);
        model.addAttribute("activeMenu", "products");
        return "seller/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @RequestParam UUID shopId,
                         @ModelAttribute ProductRequest request,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        verifyShopOwnership(shopId, userDetails.getId());
        request.setShopId(shopId.toString());
        productService.update(id.toString(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật sản phẩm thành công");
        return "redirect:/seller/products?shopId=" + shopId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id,
                         @RequestParam UUID shopId,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            verifyShopOwnership(shopId, userDetails.getId());
            productService.deleteProductByShop(id, shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/seller/products?shopId=" + shopId;
    }

    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("=== UPLOAD DEBUG: file=" + (file != null ? file.getOriginalFilename() : "null") + " size=" + (file != null ? file.getSize() : 0));
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File rỗng"));
        }

        System.out.println("=== UPLOAD DEBUG: uploadPath=" + productUploadConfig.getUploadPath());
        String url = FileUploadUtil.saveFile(
                file,
                productUploadConfig.getUploadPath(),
                productUploadConfig.getUrlPrefix()
        );

        System.out.println("=== UPLOAD DEBUG: saved url=" + url);
        return ResponseEntity.ok(Map.of("url", url));
    }

    private void verifyShopOwnership(UUID shopId, UUID userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", shopId.toString()));
        if (!shop.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền quản lý shop này");
        }
    }
}
