package com.ptithcm.smartshop.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProductRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    
    private String description;
    
    private Boolean status;
    
    @NotBlank(message = "Mã danh mục không được để trống")
    private String categoryId;

    public ProductRequest() {
    }

    public ProductRequest(String name, String description, Boolean status, String categoryId) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
