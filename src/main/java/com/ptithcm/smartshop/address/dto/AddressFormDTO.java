package com.ptithcm.smartshop.address.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressFormDTO {

    private UUID id;

    @NotBlank(message = "Vui lòng nhập họ và tên người nhận")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và gồm đúng 10 chữ số")
    private String phone;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String province;

    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String district;

    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String ward;

    @NotBlank(message = "Vui lòng nhập số nhà, tên đường")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
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
        this.receiverName = receiverName != null ? receiverName.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province != null ? province.trim() : null;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district != null ? district.trim() : null;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward != null ? ward.trim() : null;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail != null ? detail.trim() : null;
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