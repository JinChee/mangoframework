package org.mangoframework.core.annotation;


/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public @interface RequestMapping {

    String value() default "";

    boolean get() default false;

    boolean post() default false;

    boolean put() default false;

    boolean delete() default false;

    boolean singleton() default false;
}
