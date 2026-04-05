package com.ptithcm.smartshop.auth.annotation;

import com.ptithcm.smartshop.auth.enums.AuthRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

	AuthRole value();
}
