package com.ptithcm.smartshop.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminImageForm {
    @NotBlank(message = "URL ảnh không được để trống")
    private String imageUrl;
    @NotNull(message = "Ảnh chính không được để trống")
    private Boolean main;
    private Integer sortOrder;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getMain() { return main; }
    public void setMain(Boolean main) { this.main = main; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
