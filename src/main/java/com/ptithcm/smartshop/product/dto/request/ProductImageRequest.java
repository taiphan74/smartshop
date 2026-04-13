package com.ptithcm.smartshop.product.dto.request;

public class ProductImageRequest {
    
    private String productId;
    private String imageUrl;
    private Boolean isMain;
    private Integer sortOrder;

    public ProductImageRequest() {
    }

    public ProductImageRequest(String productId, String imageUrl, Boolean isMain, Integer sortOrder) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.sortOrder = sortOrder;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}

