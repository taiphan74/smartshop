package com.ptithcm.smartshop.address.dto;

import java.util.UUID;

public class AddressFormDTO {

    private UUID id;
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detail;
    private boolean isDefault;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isComplete() {
        return hasText(receiverName)
                && hasText(phone)
                && hasText(province)
                && hasText(district)
                && hasText(ward)
                && hasText(detail);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}