package com.ptithcm.smartshop.security.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

	Permission[] value() default {};

	Permission[] anyOf() default {};

	Permission[] allOf() default {};
}
