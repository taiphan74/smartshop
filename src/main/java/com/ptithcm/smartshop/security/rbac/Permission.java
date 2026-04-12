package com.ptithcm.smartshop.security.rbac;

public enum Permission {
	USER_READ("user:read"),
	USER_CREATE("user:create"),
	USER_UPDATE("user:update"),
	USER_DELETE("user:delete"),
	SHOP_READ("shop:read"),
	SHOP_CREATE("shop:create"),
	SHOP_UPDATE("shop:update"),
	PRODUCT_CREATE("product:create"),
	PRODUCT_UPDATE("product:update"),
	PRODUCT_DELETE("product:delete");

	private final String code;

	Permission(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
