package com.ptithcm.smartshop.promotion.controller;

import java.util.UUID;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptithcm.smartshop.admin.dto.AdminVoucherForm;
import com.ptithcm.smartshop.admin.service.AdminVoucherManagementService;
import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.security.rbac.Permission;
import com.ptithcm.smartshop.security.rbac.RequirePermission;
import com.ptithcm.smartshop.voucher.entity.VoucherDiscountType;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/promotion")
public class PromotionManagerController {

    private final AdminVoucherManagementService adminVoucherManagementService;
    private final BannerService bannerService;

    public PromotionManagerController(
            AdminVoucherManagementService adminVoucherManagementService,
            BannerService bannerService) {
        this.adminVoucherManagementService = adminVoucherManagementService;
        this.bannerService = bannerService;
    }

    @GetMapping("/vouchers")
    @RequirePermission(Permission.VOUCHER_READ)
    public String listVouchers(@RequestParam(required = false) String editId, Model model) {
        model.addAttribute("activeMenu", "vouchers");
        model.addAttribute("vouchers", adminVoucherManagementService.findAll());
        model.addAttribute("scopes", VoucherScope.values());
        model.addAttribute("discountTypes", VoucherDiscountType.values());

        if (!model.containsAttribute("voucherForm")) {
            model.addAttribute("voucherForm",
                    editId != null && !editId.isBlank()
                            ? adminVoucherManagementService.buildEditForm(editId)
                            : adminVoucherManagementService.buildCreateForm());
        }

        model.addAttribute("editing", editId != null && !editId.isBlank());
        model.addAttribute("editingId", editId);
        return "promotion/vouchers";
    }

    @PostMapping("/vouchers")
    @RequirePermission(Permission.VOUCHER_CREATE)
    public String createVoucher(
            @Valid @ModelAttribute("voucherForm") AdminVoucherForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("vouchers", adminVoucherManagementService.findAll());
            model.addAttribute("scopes", VoucherScope.values());
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("editing", false);
            return "promotion/vouchers";
        }

        try {
            adminVoucherManagementService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Tao voucher thanh cong");
            return "redirect:/promotion/vouchers";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("vouchers", adminVoucherManagementService.findAll());
            model.addAttribute("scopes", VoucherScope.values());
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("editing", false);
            model.addAttribute("errorMessage", exception.getMessage());
            return "promotion/vouchers";
        }
    }

    @PostMapping("/vouchers/{id}/update")
    @RequirePermission(Permission.VOUCHER_UPDATE)
    public String updateVoucher(
            @PathVariable String id,
            @Valid @ModelAttribute("voucherForm") AdminVoucherForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("vouchers", adminVoucherManagementService.findAll());
            model.addAttribute("scopes", VoucherScope.values());
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("editing", true);
            model.addAttribute("editingId", id);
            return "promotion/vouchers";
        }

        try {
            adminVoucherManagementService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat voucher thanh cong");
            return "redirect:/promotion/vouchers";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("vouchers", adminVoucherManagementService.findAll());
            model.addAttribute("scopes", VoucherScope.values());
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("editing", true);
            model.addAttribute("editingId", id);
            model.addAttribute("errorMessage", exception.getMessage());
            return "promotion/vouchers";
        }
    }

    @PostMapping("/vouchers/{id}/toggle-active")
    @RequirePermission(Permission.VOUCHER_UPDATE)
    public String toggleVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
        adminVoucherManagementService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat trang thai voucher");
        return "redirect:/promotion/vouchers";
    }

    @PostMapping("/vouchers/{id}/delete")
    @RequirePermission(Permission.VOUCHER_DELETE)
    public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
        adminVoucherManagementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Da xoa voucher");
        return "redirect:/promotion/vouchers";
    }

    @GetMapping("/events")
    @RequirePermission(Permission.EVENT_READ)
    public String events(@RequestParam(required = false) String editId, Model model) {
        model.addAttribute("activeMenu", "events");
        model.addAttribute("events", bannerService.findAll());

        if (!model.containsAttribute("eventForm")) {
            model.addAttribute("eventForm", buildEventForm(editId));
        }

        boolean editing = editId != null && !editId.isBlank();
        model.addAttribute("editing", editing);
        model.addAttribute("editingId", editId);
        return "promotion/events";
    }

    @PostMapping("/events")
    @RequirePermission(Permission.EVENT_CREATE)
    public String createEvent(
            @ModelAttribute("eventForm") BannerForm form,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {
        String validationError = validateEventForm(form, imageFile);
        if (validationError != null) {
            populateEventModel(model, form, false, null, validationError);
            return "promotion/events";
        }

        bannerService.create(normalizeEventForm(form, imageFile), imageFile);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo sự kiện quảng cáo thành công");
        return "redirect:/promotion/events";
    }

    @PostMapping("/events/{id}/update")
    @RequirePermission(Permission.EVENT_UPDATE)
    public String updateEvent(
            @PathVariable UUID id,
            @ModelAttribute("eventForm") BannerForm form,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {
        String validationError = validateEventForm(form, imageFile);
        if (validationError != null) {
            populateEventModel(model, form, true, id.toString(), validationError);
            return "promotion/events";
        }

        bannerService.update(id, normalizeEventForm(form, imageFile), imageFile);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sự kiện quảng cáo thành công");
        return "redirect:/promotion/events";
    }

    @PostMapping("/events/{id}/toggle-active")
    @RequirePermission(Permission.EVENT_PUBLISH)
    public String toggleEvent(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Banner eventBanner = bannerService.findById(id);
        BannerForm toggleForm = new BannerForm(
                eventBanner.getTitle(),
                eventBanner.getImageUrl(),
                null,
                eventBanner.getDisplayOrder(),
                !Boolean.TRUE.equals(eventBanner.getIsActive()));

        bannerService.update(id, toggleForm, null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hiển thị sự kiện");
        return "redirect:/promotion/events";
    }

    @PostMapping("/events/{id}/delete")
    @RequirePermission(Permission.EVENT_UPDATE)
    public String deleteEvent(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        bannerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sự kiện quảng cáo");
        return "redirect:/promotion/events";
    }

    private BannerForm buildEventForm(String editId) {
        if (editId == null || editId.isBlank()) {
            return new BannerForm(null, null, null, 0, true);
        }

        Banner eventBanner = bannerService.findById(UUID.fromString(editId));
        return new BannerForm(
                eventBanner.getTitle(),
                eventBanner.getImageUrl(),
            null,
                eventBanner.getDisplayOrder(),
                eventBanner.getIsActive());
    }

    private void populateEventModel(
            Model model,
            BannerForm form,
            boolean editing,
            String editingId,
            String errorMessage) {
        model.addAttribute("activeMenu", "events");
        model.addAttribute("events", bannerService.findAll());
        model.addAttribute("eventForm", form);
        model.addAttribute("editing", editing);
        model.addAttribute("editingId", editingId);
        model.addAttribute("errorMessage", errorMessage);
    }

    private BannerForm normalizeEventForm(BannerForm form, MultipartFile imageFile) {
        String imageUrl = trimToNull(form.imageUrl());
        if (imageUrl == null && imageFile != null && !imageFile.isEmpty()) {
            imageUrl = "uploaded";
        }

        Integer displayOrder = form.displayOrder();
        Boolean isActive = form.isActive();

        return new BannerForm(
                trimToNull(form.title()),
                imageUrl,
                null,
                displayOrder,
                isActive);
    }

    private String validateEventForm(BannerForm form, MultipartFile imageFile) {
        if (form.displayOrder() < 0) {
            return "Thứ tự hiển thị phải lớn hơn hoặc bằng 0";
        }

        boolean hasImageUpload = imageFile != null && !imageFile.isEmpty();
        boolean hasImageUrl = trimToNull(form.imageUrl()) != null;
        if (!hasImageUpload && !hasImageUrl) {
            return "Vui lòng nhập URL ảnh hoặc tải ảnh sự kiện";
        }

        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
