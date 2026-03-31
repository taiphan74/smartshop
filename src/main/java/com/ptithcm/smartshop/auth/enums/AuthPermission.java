package com.ptithcm.smartshop.auth.enums;

public enum AuthPermission {
	USER_READ,
	USER_CREATE,
	USER_UPDATE,
	USER_DELETE;

	public String code() {
		return name();
	}
}
