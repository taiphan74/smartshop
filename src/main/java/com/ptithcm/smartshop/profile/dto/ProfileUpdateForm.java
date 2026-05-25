package com.ptithcm.smartshop.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateForm(
    @NotBlank(message = "{profile.validation.full_name.not_blank}")
    @Size(min = 2, max = 50, message = "{profile.validation.full_name.size}")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    String fullName,

    @Pattern(regexp = "^$|^(03|05|07|08|09)\\d{8}$", message = "{profile.validation.phone.pattern}")
    String phone
) {}