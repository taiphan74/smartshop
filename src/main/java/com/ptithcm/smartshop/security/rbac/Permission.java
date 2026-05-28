package com.ptithcm.smartshop.security.rbac;

public enum Permission {
	USER_READ("user:read"),
	USER_CREATE("user:create"),
	USER_UPDATE("user:update"),
	USER_DELETE("user:delete"),
	BANNER_READ("banner:read"),
	BANNER_CREATE("banner:create"),
	BANNER_UPDATE("banner:update"),
	BANNER_DELETE("banner:delete"),
	VOUCHER_READ("voucher:read"),
	VOUCHER_CREATE("voucher:create"),
	VOUCHER_UPDATE("voucher:update"),
	VOUCHER_DELETE("voucher:delete"),
	EVENT_READ("event:read"),
	EVENT_CREATE("event:create"),
	EVENT_UPDATE("event:update"),
	EVENT_PUBLISH("event:publish");

	private final String code;

	Permission(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
