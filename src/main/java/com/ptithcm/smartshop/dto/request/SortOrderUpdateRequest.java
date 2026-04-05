package com.ptithcm.smartshop.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SortOrderUpdateRequest {

    @NotBlank(message = "ID không được để trống")
    private String id;

    private Integer sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
