package com.ptithcm.smartshop.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateForm(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 20) String phone
) {
}
