package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.service.AdminReviewManagementService;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final AdminReviewManagementService reviewManagementService;

    public AdminReviewController(AdminReviewManagementService reviewManagementService) {
        this.reviewManagementService = reviewManagementService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(
            @RequestParam(required = false) Boolean visible,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("reviews", reviewManagementService.list(
                visible,
                PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("selectedVisible", visible);
        return "admin/reviews/list";
    }

    @GetMapping("/{reviewId}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable UUID reviewId, Model model) {
        model.addAttribute("review", reviewManagementService.get(reviewId));
        return "admin/reviews/detail";
    }

    @PostMapping("/{reviewId}/hide")
    public String hide(@PathVariable UUID reviewId, RedirectAttributes redirectAttributes) {
        return mutate(reviewId, redirectAttributes, () -> reviewManagementService.hide(reviewId), "Đã ẩn đánh giá");
    }

    @PostMapping("/{reviewId}/show")
    public String show(@PathVariable UUID reviewId, RedirectAttributes redirectAttributes) {
        return mutate(reviewId, redirectAttributes, () -> reviewManagementService.show(reviewId), "Đã hiện đánh giá");
    }

    @PostMapping("/{reviewId}/delete")
    public String delete(@PathVariable UUID reviewId, RedirectAttributes redirectAttributes) {
        return mutate(reviewId, redirectAttributes, () -> reviewManagementService.delete(reviewId), "Đã xóa đánh giá");
    }

    private String mutate(UUID reviewId, RedirectAttributes redirectAttributes, Runnable action, String successMessage) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/reviews";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/reviews/" + reviewId;
        }
    }
}
