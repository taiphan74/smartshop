package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.dto.AdminImageForm;
import com.ptithcm.smartshop.admin.dto.AdminProductForm;
import com.ptithcm.smartshop.admin.dto.AdminVariantForm;
import com.ptithcm.smartshop.admin.service.AdminProductManagementService;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductManagementService productManagementService;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    public AdminProductController(
            AdminProductManagementService productManagementService,
            CategoryRepository categoryRepository,
            ShopRepository shopRepository) {
        this.productManagementService = productManagementService;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(@RequestParam(required = false) Boolean status, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("products", productManagementService.list(status, PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("selectedStatus", status);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new AdminProductForm());
        addProductFormOptions(model);
        return "admin/products/form";
    }

    @PostMapping
    public String create(@Valid AdminProductForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addProductFormOptions(model);
            return "admin/products/form";
        }
        try {
            Product product = productManagementService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo sản phẩm");
            return "redirect:/admin/products/" + product.getId();
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("product", ex.getMessage());
            addProductFormOptions(model);
            return "admin/products/form";
        }
    }

    @GetMapping("/{productId}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable UUID productId, Model model) {
        model.addAttribute("product", productManagementService.get(productId));
        model.addAttribute("variants", productManagementService.variants(productId));
        model.addAttribute("images", productManagementService.images(productId));
        model.addAttribute("variantForm", new AdminVariantForm());
        model.addAttribute("imageForm", new AdminImageForm());
        return "admin/products/detail";
    }

    @GetMapping("/{productId}/edit")
    @Transactional(readOnly = true)
    public String editForm(@PathVariable UUID productId, Model model) {
        Product product = productManagementService.get(productId);
        AdminProductForm form = new AdminProductForm();
        form.setName(product.getName());
        form.setSlug(product.getSlug());
        form.setDescription(product.getDescription());
        form.setStatus(product.getStatus());
        form.setCategoryId(product.getCategory().getId());
        form.setShopId(product.getShop().getId());
        model.addAttribute("product", product);
        model.addAttribute("form", form);
        addProductFormOptions(model);
        return "admin/products/form";
    }

    @PostMapping("/{productId}/edit")
    public String update(@PathVariable UUID productId, @Valid AdminProductForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", productManagementService.get(productId));
            addProductFormOptions(model);
            return "admin/products/form";
        }
        try {
            productManagementService.update(productId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật sản phẩm");
            return "redirect:/admin/products/" + productId;
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("product", ex.getMessage());
            model.addAttribute("product", productManagementService.get(productId));
            addProductFormOptions(model);
            return "admin/products/form";
        }
    }

    @PostMapping("/{productId}/hide")
    public String hide(@PathVariable UUID productId, RedirectAttributes redirectAttributes) {
        productManagementService.hide(productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn sản phẩm");
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/show")
    public String show(@PathVariable UUID productId, RedirectAttributes redirectAttributes) {
        productManagementService.show(productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hiện sản phẩm");
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/delete")
    public String delete(@PathVariable UUID productId, RedirectAttributes redirectAttributes) {
        try {
            productManagementService.delete(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products/" + productId;
        }
    }

    @PostMapping("/{productId}/variants")
    public String createVariant(@PathVariable UUID productId, @Valid AdminVariantForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                throw new IllegalArgumentException("Vui lòng kiểm tra thông tin phiên bản");
            }
            productManagementService.createVariant(productId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm phiên bản");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable UUID productId, @PathVariable UUID variantId, RedirectAttributes redirectAttributes) {
        productManagementService.deleteVariant(productId, variantId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phiên bản");
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/images")
    public String createImage(@PathVariable UUID productId, @Valid AdminImageForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                throw new IllegalArgumentException("Vui lòng kiểm tra thông tin ảnh");
            }
            productManagementService.createImage(productId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm ảnh");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable UUID productId, @PathVariable UUID imageId, RedirectAttributes redirectAttributes) {
        productManagementService.deleteImage(productId, imageId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa ảnh");
        return "redirect:/admin/products/" + productId;
    }

    private void addProductFormOptions(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("shops", shopRepository.findAll());
    }
}
