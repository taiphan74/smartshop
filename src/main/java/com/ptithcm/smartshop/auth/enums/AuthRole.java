package com.ptithcm.smartshop.auth.enums;

public enum AuthRole {
	ADMIN,
	CUSTOMER,
	SELLER;

	public String code() {
		return name();
	}

	public String authority() {
		return "ROLE_" + name();
	}
}
