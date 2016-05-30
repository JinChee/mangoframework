package org.mangoframework.core.tag;

import org.apache.log4j.Logger;
import org.mangoframework.core.exception.MangoException;
import org.mangoframework.core.dispatcher.Parameter;
import org.mangoframework.core.dispatcher.ServiceHandler;
import org.mangoframework.core.view.RequestContextView;
import org.mangoframework.core.view.ResultView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author: zhoujingjie
 * @Date: 16/4/22
 */
public class RequestTagSupport extends TagSupport {

    private static Logger log = Logger.getLogger(RequestTagSupport.class);
    private String contextKey;

    private String controller;

    private String args;

    @Override
    public int doStartTag() throws JspException {
        try {
            ServiceHandler sh = ServiceHandler.getInstance();
            Parameter parameter = sh.initializeParameter((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
            if(args!=null && args.length()>0){
                String[] nvps = args.split("&");
                for(String nvp:nvps){
                    String[] nvs = nvp.split("=");
                    if(nvs.length == 2){
                        parameter.getParamString().put(nvs[0],nvs[1]);
                    }
                }
            }
            parameter.setPath(controller);
            ResultView view = sh.handleRequest(parameter);
            if(view !=null){
                new RequestContextView(contextKey,view.getData()).doRepresent(parameter);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
        }
        return super.doStartTag();
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
