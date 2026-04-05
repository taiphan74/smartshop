package com.ptithcm.smartshop.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProductOptionValueRequest {

    @NotBlank(message = "Mã option không được để trống")
    private String optionId;

    @NotBlank(message = "Giá trị option không được để trống")
    private String value;

    private Integer sortOrder;

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}

