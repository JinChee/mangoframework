package org.mangoframework.core.dispatcher;

import org.mangoframework.core.annotation.RequestMapping;

import java.lang.reflect.Method;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class Controller {
    private Object instance;
    private Method method;
    private RequestMapping requestMapping;

    public Controller(Object instance, Method method, RequestMapping requestMapping) {
        this.instance = instance;
        this.method = method;
        this.requestMapping = requestMapping;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public RequestMapping getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(RequestMapping requestMapping) {
        this.requestMapping = requestMapping;
    }
}
