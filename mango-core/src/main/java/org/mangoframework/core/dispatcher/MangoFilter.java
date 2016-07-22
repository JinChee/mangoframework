package org.mangoframework.core.dispatcher;

import java.lang.reflect.Method;

/**
 * @author zhoujingjie
 * @date 2016-06-07
 */
public interface MangoFilter {
    boolean doFilter(Parameter parameter, Method method) throws Exception;
}
