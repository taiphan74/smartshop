package com.ptithcm.smartshop.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_option_values")
public class ProductOptionValue {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductOption getOption() {
        return option;
    }

    public void setOption(ProductOption option) {
        this.option = option;
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
