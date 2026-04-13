package com.ptithcm.smartshop.auth.annotation;

import com.ptithcm.smartshop.auth.enums.AuthRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
/**
 * Annotation dùng để khai báo role bắt buộc cho class hoặc method.
 *
 * AuthorizationAspect sẽ đọc annotation này ở runtime để kiểm tra quyền truy
 * cập.
 */
public @interface RequireRole {

	/**
	 * Role bắt buộc phải có để được phép truy cập.
	 *
	 * @return role thuộc enum AuthRole
	 */
	AuthRole value();
}
