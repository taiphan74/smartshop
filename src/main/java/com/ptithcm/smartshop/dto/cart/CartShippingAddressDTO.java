package com.ptithcm.smartshop.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CartShippingAddressDTO {

    @NotBlank(message = "Vui lòng nhập họ và tên người nhận")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và gồm đúng 10 chữ số")
    private String phone;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String province;

    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String district;

    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String ward;

    @NotBlank(message = "Vui lòng nhập địa chỉ chi tiết")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String detailAddress;

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName != null ? recipientName.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress != null ? detailAddress.trim() : null;
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

    public boolean hasStructuredAddress() {
        return hasText(province) && hasText(district) && hasText(ward) && hasText(detailAddress);
    }

    public String getFullAddress() {
        if (!hasStructuredAddress()) {
            return detailAddress;
        }
        return String.join(", ", detailAddress, ward, district, province);
    }

    public boolean isComplete() {
        return hasText(recipientName) && hasText(phone) && hasText(detailAddress);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}