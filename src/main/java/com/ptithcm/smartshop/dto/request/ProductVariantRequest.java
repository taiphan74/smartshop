package com.ptithcm.smartshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productId;

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private Boolean status;
    private Double weight;
    private String thumbnailUrl;

    @NotEmpty(message = "Danh sách option value không được để trống")
    private List<String> optionValueIds;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public void setCompareAtPrice(BigDecimal compareAtPrice) {
        this.compareAtPrice = compareAtPrice;
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<String> getOptionValueIds() {
        return optionValueIds;
    }

    public void setOptionValueIds(List<String> optionValueIds) {
        this.optionValueIds = optionValueIds;
    }
}
