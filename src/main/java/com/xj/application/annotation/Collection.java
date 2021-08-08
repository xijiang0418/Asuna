package com.xj.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Collection {

    Class type() default Object.class;

    String primaryID() default "";

    String foreignID() default "";

    String nextBeanID() default "";
}
