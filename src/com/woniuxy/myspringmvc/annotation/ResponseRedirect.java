package com.woniuxy.myspringmvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseRedirect {
	//默认使用相对路径，绝对路径设置为true
	boolean value() default false;
}
