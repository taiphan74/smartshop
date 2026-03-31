package com.ptithcm.smartshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    
    private String slug;
    
    private String description;
    
    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", message = "Giá sản phẩm không được nhỏ hơn 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Số lượng trong kho không được nhỏ hơn 0")
    private Integer stockQuantity;
    
    private Boolean status;
    
    @NotBlank(message = "Mã danh mục không được để trống")
    private String categoryId;

    public ProductRequest() {
    }

    public ProductRequest(String name, String slug, String description, BigDecimal price, Integer stockQuantity,
                          Boolean status, String categoryId) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
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
