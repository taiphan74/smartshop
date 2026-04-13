package com.ptithcm.smartshop.banner.controller;

import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.security.rbac.Permission;
import com.ptithcm.smartshop.security.rbac.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    @RequirePermission(Permission.BANNER_READ)
    public String list(Model model) {
        List<Banner> banners = bannerService.findAll();
        model.addAttribute("banners", banners);
        model.addAttribute("bannerForm", new BannerForm(null, null, null, 0, true));
        return "admin/banners/list";
    }

    @PostMapping("/create")
    @RequirePermission(Permission.BANNER_CREATE)
    public String create(
            @ModelAttribute("bannerForm") @Valid BannerForm form,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        if (result.hasErrors() || ((imageFile == null || imageFile.isEmpty()) && (form.imageUrl() == null || form.imageUrl().isBlank()))) {
            model.addAttribute("banners", bannerService.findAll());
            return "admin/banners/list";
        }

        bannerService.create(form, imageFile);
        return "redirect:/admin/banners";
    }

    @PostMapping("/update/{id}")
    @RequirePermission(Permission.BANNER_UPDATE)
    public String update(
            @PathVariable UUID id,
            @ModelAttribute("bannerForm") @Valid BannerForm form,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("banners", bannerService.findAll());
            model.addAttribute("editingId", id);
            return "admin/banners/list";
        }

        bannerService.update(id, form, imageFile);
        return "redirect:/admin/banners";
    }

    @PostMapping("/delete/{id}")
    @RequirePermission(Permission.BANNER_DELETE)
    public String delete(@PathVariable UUID id) {
        bannerService.delete(id);
        return "redirect:/admin/banners";
    }
}
