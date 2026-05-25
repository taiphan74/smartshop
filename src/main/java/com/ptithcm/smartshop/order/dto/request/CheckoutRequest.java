package com.ptithcm.smartshop.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và gồm đúng 10 chữ số")
    private String customerPhone;

    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String customerAddress;

    @Size(max = 100, message = "Tỉnh/thành phố không được vượt quá 100 ký tự")
    private String province;

    @Size(max = 100, message = "Quận/huyện không được vượt quá 100 ký tự")
    private String district;

    @Size(max = 100, message = "Phường/xã không được vượt quá 100 ký tự")
    private String ward;

    @Size(max = 255, message = "Số nhà, tên đường không được vượt quá 255 ký tự")
    private String detailAddress;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName != null ? customerName.trim() : null;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone != null ? customerPhone.trim() : null;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress != null ? customerAddress.trim() : null;
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

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress != null ? detailAddress.trim() : null;
    }
}