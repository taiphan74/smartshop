package com.ptithcm.smartshop.shop.dto;

import com.ptithcm.smartshop.shop.enums.ShopStatus;

public record ShopSummary(
        String name,
        String slug,
        String description,
        String phone,
        ShopStatus status
) {
}
