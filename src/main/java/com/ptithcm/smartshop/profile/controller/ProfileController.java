package com.ptithcm.smartshop.profile.controller;

import com.ptithcm.smartshop.profile.dto.ProfileUpdateForm;
import com.ptithcm.smartshop.profile.dto.ShopRegistrationForm;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.shop.service.ShopRegistrationService;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final ShopRegistrationService shopRegistrationService;

    public ProfileController(UserRepository userRepository, ShopRegistrationService shopRegistrationService) {
        this.userRepository = userRepository;
        this.shopRegistrationService = shopRegistrationService;
    }

    @GetMapping("/profile")
    public String profile(@SessionAttribute(name = SessionConstants.CURRENT_USER, required = false) SessionUser sessionUser, Model model) {
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findById(sessionUser.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("profileForm", new ProfileUpdateForm(user.getFullName(), user.getPhone()));
        model.addAttribute("shopForm", new ShopRegistrationForm("", "", "", ""));
        model.addAttribute("shops", shopRegistrationService.findOwnedShopSummaries(sessionUser.id()));
        return "profile/index";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @SessionAttribute(name = SessionConstants.CURRENT_USER, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("profileForm") ProfileUpdateForm form,
            RedirectAttributes redirectAttributes) {
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findById(sessionUser.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(form.fullName().trim());
        user.setPhone(form.phone() == null || form.phone().isBlank() ? null : form.phone().trim());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/shops")
    public String registerShop(
            @SessionAttribute(name = SessionConstants.CURRENT_USER, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("shopForm") ShopRegistrationForm form,
            RedirectAttributes redirectAttributes) {
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }
        shopRegistrationService.register(sessionUser.id(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã gửi đăng ký shop. Vui lòng chờ admin duyệt.");
        return "redirect:/profile";
    }

    @GetMapping("/profile/shops/register")
    public String viewShopRegister(Model model) {
            if (!model.containsAttribute("shopForm")) {
        model.addAttribute("shopForm", new ShopRegistrationForm("", "", "", ""));
    }
        return "profile/shops/register"; 
    }

}
