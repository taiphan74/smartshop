package com.ptithcm.smartshop.dto;

import java.util.List;

public class ProductOptionDTO {
    private String id;
    private String name;
    private Integer sortOrder;
    private List<ProductOptionValueDTO> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<ProductOptionValueDTO> getValues() {
        return values;
    }

    public void setValues(List<ProductOptionValueDTO> values) {
        this.values = values;
    }
}
