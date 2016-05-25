package org.mangoframework.core.dispatcher;

/**
 * @author zhoujingjie
 * @date 2016-05-25
 */
public class RequestMapping {
    private boolean singleton;
    private String template;

    public RequestMapping(boolean singleton, String template) {
        this.singleton = singleton;
        this.template = template;
    }

    public RequestMapping() {
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
