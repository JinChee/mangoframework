package org.mangoframework.core.dispatcher;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class Controller {
    private Object instance;
    private Method method;
    private RequestMapping requestMapping;
    private Map<String,String> pathMap = new HashMap<>();

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

    public Map<String, String> getPathMap() {
        return pathMap;
    }

    public void setPathMap(Map<String, String> pathMap) {
        this.pathMap = pathMap;
    }

}
