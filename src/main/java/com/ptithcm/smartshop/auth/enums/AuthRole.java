package com.ptithcm.smartshop.auth.enums;

/**
 * Danh sách role chuẩn của hệ thống cho cơ chế xác thực/ủy quyền.
 */
public enum AuthRole {
	ADMIN,
	CUSTOMER,
	SELLER;

	/**
	 * Trả về mã role theo chuẩn lưu trữ (dùng trực tiếp name của enum).
	 *
	 * @return mã role
	 */
	public String code() {
		return name();
	}

	/**
	 * Trả về authority theo chuẩn Spring Security (prefix ROLE_).
	 *
	 * @return authority role
	 */
	public String authority() {
		return "ROLE_" + name();
	}
}
