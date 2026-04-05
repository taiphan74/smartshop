package com.ptithcm.smartshop.product.dto;

public class ProductImageDTO {
    
    private String id;
    private String productId;
    private String imageUrl;
    private Boolean isMain;
    private Integer sortOrder;

    public ProductImageDTO() {
    }

    public ProductImageDTO(String id, String productId, String imageUrl, Boolean isMain, Integer sortOrder) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

