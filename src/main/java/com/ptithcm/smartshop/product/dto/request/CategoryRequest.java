package com.ptithcm.smartshop.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    private String parentId;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

