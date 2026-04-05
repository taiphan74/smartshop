package com.ptithcm.smartshop.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProductOptionRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productId;

    @NotBlank(message = "Tên option không được để trống")
    private String name;

    private Integer sortOrder;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
