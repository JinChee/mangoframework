package org.mangoframework.core.dispatcher;

/**
 * @author zhoujingjie
 * @date 2016-06-07
 */
public interface MangoFilter {
    boolean doFilter(Parameter parameter);
}
