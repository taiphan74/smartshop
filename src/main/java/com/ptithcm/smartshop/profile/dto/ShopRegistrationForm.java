package com.ptithcm.smartshop.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShopRegistrationForm(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @Size(max = 20) String phone,
        @Size(max = 255) String address
) {
}
