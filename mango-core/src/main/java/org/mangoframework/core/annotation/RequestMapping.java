package org.mangoframework.core.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {

    String value() default "";

    boolean get() default true;

    boolean post() default false;

    boolean put() default false;

    boolean delete() default false;

    boolean singleton() default false;

    String template() default "";
}
