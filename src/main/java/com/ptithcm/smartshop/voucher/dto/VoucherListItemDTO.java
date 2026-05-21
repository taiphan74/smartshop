package com.ptithcm.smartshop.voucher.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ptithcm.smartshop.voucher.entity.VoucherDiscountType;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;

public class VoucherListItemDTO {

    private String code;
    private String name;
    private VoucherScope scope;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private LocalDateTime endAt;
    private boolean eligible;
    private String ineligibilityReason;
    private boolean applied;
    private BigDecimal calculatedDiscount;

    public VoucherListItemDTO() {
    }

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

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getIneligibilityReason() {
        return ineligibilityReason;
    }

    public void setIneligibilityReason(String ineligibilityReason) {
        this.ineligibilityReason = ineligibilityReason;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public BigDecimal getCalculatedDiscount() {
        return calculatedDiscount;
    }

    public void setCalculatedDiscount(BigDecimal calculatedDiscount) {
        this.calculatedDiscount = calculatedDiscount;
    }

    /** Formatted label for discount e.g. "Giảm 50.000₫" or "Giảm 10% tối đa 80.000₫" */
    public String getDiscountLabel() {
        if (discountType == VoucherDiscountType.FIXED) {
            return "Giảm " + formatAmount(discountValue) + "₫";
        }
        String label = "Giảm " + discountValue.stripTrailingZeros().toPlainString() + "%";
        if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
            label += " tối đa " + formatAmount(maxDiscountAmount) + "₫";
        }
        return label;
    }

    /** Formatted condition e.g. "Cho đơn hàng từ 500K" */
    public String getConditionLabel() {
        if (minOrderValue == null || minOrderValue.compareTo(BigDecimal.ZERO) <= 0) {
            return "Không yêu cầu giá trị tối thiểu";
        }
        return "Cho đơn hàng từ " + formatAmount(minOrderValue) + "₫";
    }

    /** Formatted expiry e.g. "HSD: 30/04/26" */
    public String getExpiryLabel() {
        if (endAt == null) {
            return "Không giới hạn";
        }
        return "HSD: " + endAt.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        long value = amount.longValue();
        if (value >= 1_000_000) {
            return (value / 1_000_000) + " triệu";
        }
        if (value % 1000 == 0 && value >= 1000) {
            return (value / 1000) + "K";
        }
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
        return nf.format(value);
    }
}
