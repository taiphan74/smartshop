package com.ptithcm.smartshop.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {

    @NotBlank(message = "Vui long nhap ho va ten")
    private String customerName;

    @NotBlank(message = "Vui long nhap so dien thoai")
    private String customerPhone;

    @NotBlank(message = "Vui long nhap dia chi giao hang")
    private String customerAddress;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
}