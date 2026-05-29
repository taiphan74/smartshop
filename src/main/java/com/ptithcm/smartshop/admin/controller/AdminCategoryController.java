package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.dto.AdminCategoryForm;
import com.ptithcm.smartshop.admin.service.AdminCategoryManagementService;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryManagementService categoryManagementService;
    private final CategoryRepository categoryRepository;

    public AdminCategoryController(AdminCategoryManagementService categoryManagementService, CategoryRepository categoryRepository) {
        this.categoryManagementService = categoryManagementService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("categories", categoryManagementService.list(PageRequest.of(Math.max(page, 0), 50, Sort.by("path"))));
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        AdminCategoryForm form = new AdminCategoryForm();
        form.setLevel(0);
        model.addAttribute("form", form);
        addOptions(model);
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid AdminCategoryForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addOptions(model);
            return "admin/categories/form";
        }
        try {
            categoryManagementService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo danh mục");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("category", ex.getMessage());
            addOptions(model);
            return "admin/categories/form";
        }
    }

    @GetMapping("/{categoryId}/edit")
    public String editForm(@PathVariable UUID categoryId, Model model) {
        Category category = categoryManagementService.get(categoryId);
        AdminCategoryForm form = new AdminCategoryForm();
        form.setName(category.getName());
        form.setSlug(category.getSlug());
        form.setPath(category.getPath());
        form.setLevel(category.getLevel());
        form.setParentId(category.getParent() == null ? null : category.getParent().getId());
        model.addAttribute("category", category);
        model.addAttribute("form", form);
        addOptions(model);
        return "admin/categories/form";
    }

    @PostMapping("/{categoryId}/edit")
    public String update(@PathVariable UUID categoryId, @Valid AdminCategoryForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", categoryManagementService.get(categoryId));
            addOptions(model);
            return "admin/categories/form";
        }
        try {
            categoryManagementService.update(categoryId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh mục");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("category", ex.getMessage());
            model.addAttribute("category", categoryManagementService.get(categoryId));
            addOptions(model);
            return "admin/categories/form";
        }
    }

    @PostMapping("/{categoryId}/delete")
    public String delete(@PathVariable UUID categoryId, RedirectAttributes redirectAttributes) {
        try {
            categoryManagementService.delete(categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private void addOptions(Model model) {
        model.addAttribute("parentCategories", categoryRepository.findAll(Sort.by("path")));
    }
}
