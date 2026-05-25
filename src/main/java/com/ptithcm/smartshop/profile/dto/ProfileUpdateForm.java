package com.ptithcm.smartshop.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateForm(
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 50, message = "Họ tên phải từ 2-50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    String fullName,

    @Pattern(regexp = "^(03|05|07|08|09)\\d{8}$", message = "Số điện thoại không đúng định dạng Việt Nam (10 số)")
    String phone
) {}