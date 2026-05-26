package com.ptithcm.smartshop.shop.dto;

import com.ptithcm.smartshop.shop.enums.ShopStatus;
import java.util.UUID;

public record ShopSummary(
        UUID id,
        String name,
        String slug,
        String description,
        String phone,
        ShopStatus status
) {
}
