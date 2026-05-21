package com.ptithcm.smartshop.dto.cart;

public class CartShippingAddressDTO {

    private String recipientName;
    private String phone;
    private String detailAddress;

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public boolean isComplete() {
        return hasText(recipientName) && hasText(phone) && hasText(detailAddress);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}