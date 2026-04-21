package com.ptithcm.smartshop.dto.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDTO {

    private List<CartItemDTO> items = new ArrayList<>();
    private BigDecimal subTotal = BigDecimal.ZERO;
    private BigDecimal shippingFee = new BigDecimal("35000");
    private BigDecimal totalBeforeDiscount = BigDecimal.ZERO;
    private BigDecimal orderDiscountAmount = BigDecimal.ZERO;
    private BigDecimal shippingDiscountAmount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String appliedOrderVoucherCode;
    private String appliedOrderVoucherName;
    private String appliedShippingVoucherCode;
    private String appliedShippingVoucherName;

    public CartDTO() {
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getTotalBeforeDiscount() {
        return totalBeforeDiscount;
    }

    public void setTotalBeforeDiscount(BigDecimal totalBeforeDiscount) {
        this.totalBeforeDiscount = totalBeforeDiscount;
    }

    public BigDecimal getOrderDiscountAmount() {
        return orderDiscountAmount;
    }

    public void setOrderDiscountAmount(BigDecimal orderDiscountAmount) {
        this.orderDiscountAmount = orderDiscountAmount;
    }

    public BigDecimal getShippingDiscountAmount() {
        return shippingDiscountAmount;
    }

    public void setShippingDiscountAmount(BigDecimal shippingDiscountAmount) {
        this.shippingDiscountAmount = shippingDiscountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getAppliedOrderVoucherCode() {
        return appliedOrderVoucherCode;
    }

    public void setAppliedOrderVoucherCode(String appliedOrderVoucherCode) {
        this.appliedOrderVoucherCode = appliedOrderVoucherCode;
    }

    public String getAppliedOrderVoucherName() {
        return appliedOrderVoucherName;
    }

    public void setAppliedOrderVoucherName(String appliedOrderVoucherName) {
        this.appliedOrderVoucherName = appliedOrderVoucherName;
    }

    public String getAppliedShippingVoucherCode() {
        return appliedShippingVoucherCode;
    }

    public void setAppliedShippingVoucherCode(String appliedShippingVoucherCode) {
        this.appliedShippingVoucherCode = appliedShippingVoucherCode;
    }

    public String getAppliedShippingVoucherName() {
        return appliedShippingVoucherName;
    }

    public void setAppliedShippingVoucherName(String appliedShippingVoucherName) {
        this.appliedShippingVoucherName = appliedShippingVoucherName;
    }

    public BigDecimal getTotalDiscountAmount() {
        return orderDiscountAmount.add(shippingDiscountAmount);
    }

    public boolean hasOrderDiscount() {
        return orderDiscountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasShippingDiscount() {
        return shippingDiscountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasAppliedOrderVoucher() {
        return appliedOrderVoucherCode != null && !appliedOrderVoucherCode.isBlank();
    }

    public boolean hasAppliedShippingVoucher() {
        return appliedShippingVoucherCode != null && !appliedShippingVoucherCode.isBlank();
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }
}
