package com.ptithcm.smartshop.security.rbac;

public enum Permission {
	USER_READ("user:read"),
	USER_CREATE("user:create"),
	USER_UPDATE("user:update"),
	USER_DELETE("user:delete");

	private final String code;

	Permission(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
