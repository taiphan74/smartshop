package com.ptithcm.smartshop.shop.service;

import com.ptithcm.smartshop.profile.dto.ShopRegistrationForm;
import com.ptithcm.smartshop.shop.dto.ShopSummary;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ShopRegistrationService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ShopRegistrationService(ShopRepository shopRepository, UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Shop register(UUID userId, ShopRegistrationForm form) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Shop shop = new Shop();
        shop.setOwner(owner);
        shop.setName(form.name().trim());
        shop.setSlug(uniqueSlug(form.name()));
        shop.setDescription(blankToNull(form.description()));
        shop.setPhone(blankToNull(form.phone()));
        shop.setStatus(ShopStatus.PENDING);
        return shopRepository.save(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopSummary> findOwnedShopSummaries(UUID userId) {
        return shopRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(shop -> new ShopSummary(
                        shop.getId(),
                        shop.getName(),
                        shop.getSlug(),
                        shop.getDescription(),
                        shop.getPhone(),
                        shop.getStatus()))
                .toList();
    }

    private String uniqueSlug(String name) {
        String base = slugify(name);
        String candidate = base;
        int suffix = 2;
        while (shopRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "shop" : normalized;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
