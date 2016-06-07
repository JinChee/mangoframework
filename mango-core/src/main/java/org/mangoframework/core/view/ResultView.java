package org.mangoframework.core.view;

import org.mangoframework.core.dispatcher.Parameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public abstract class ResultView {

    private Object data;
    private String template;

    public abstract void doRepresent(Parameter parameter) throws Exception;

    public void handleException(Parameter parameter,Throwable e) throws Exception {
        e.printStackTrace();
    }

    public ResultView() {
    }

    public ResultView(Object data, String template) {
        this.data = data;
        this.template = template;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
