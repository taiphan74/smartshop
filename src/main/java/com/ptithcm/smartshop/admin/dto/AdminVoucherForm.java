package com.ptithcm.smartshop.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ptithcm.smartshop.voucher.entity.VoucherDiscountType;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminVoucherForm {

    @NotBlank(message = "Ma voucher khong duoc de trong")
    @Size(max = 50, message = "Ma voucher toi da 50 ky tu")
    private String code;

    @NotBlank(message = "Ten voucher khong duoc de trong")
    @Size(max = 255, message = "Ten voucher toi da 255 ky tu")
    private String name;

    @NotNull(message = "Pham vi ap dung khong duoc de trong")
    private VoucherScope scope;

    @NotNull(message = "Loai giam khong duoc de trong")
    private VoucherDiscountType discountType;

    @NotNull(message = "Gia tri giam khong duoc de trong")
    @DecimalMin(value = "0.01", message = "Gia tri giam phai lon hon 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giam toi da khong hop le")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Don toi thieu khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Don toi thieu khong hop le")
    private BigDecimal minOrderValue;

    @Min(value = 1, message = "Gioi han luot dung phai tu 1")
    private Integer usageLimit;

    private boolean active = true;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VoucherScope getScope() {
        return scope;
    }

    public void setScope(VoucherScope scope) {
        this.scope = scope;
    }

    public VoucherDiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(VoucherDiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
