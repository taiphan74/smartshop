package com.ptithcm.smartshop.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BannerForm(
        String title,

        @NotBlank(message = "Image is required")
        String imageUrl,

        String linkUrl,

        @NotNull(message = "Display order is required")
        Integer displayOrder,

        Boolean isActive
) {
    public BannerForm {
        if (displayOrder == null) displayOrder = 0;
        if (isActive == null) isActive = true;
    }
}
