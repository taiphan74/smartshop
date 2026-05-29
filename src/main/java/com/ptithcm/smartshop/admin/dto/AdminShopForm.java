package com.ptithcm.smartshop.admin.dto;

import com.ptithcm.smartshop.shop.enums.ShopStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminShopForm {

    @NotBlank(message = "Tên shop không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String logoUrl;
    private String bannerUrl;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;
    private String description;

    @NotNull(message = "Trạng thái không được để trống")
    private ShopStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ShopStatus getStatus() { return status; }
    public void setStatus(ShopStatus status) { this.status = status; }
}
