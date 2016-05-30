package org.mangoframework.core.view;

import org.mangoframework.core.dispatcher.Parameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author: zhoujingjie
 * @Date: 16/4/24
 */
public class RequestContextView extends ResultView{
    private Object data;
    private String contextKey;
    public RequestContextView(String contextKey,Object data){
        this.data = data;
        this.contextKey = contextKey;
    }

    @Override
    public void doRepresent(Parameter parameter) throws Exception {
        parameter.getRequest().setAttribute(contextKey,data);
    }
}
