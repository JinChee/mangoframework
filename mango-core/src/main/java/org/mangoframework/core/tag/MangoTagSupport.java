package org.mangoframework.core.tag;

import org.apache.log4j.Logger;
import org.mangoframework.core.MangoDispatcherHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author: zhoujingjie
 * @Date: 16/4/22
 */
public class MangoTagSupport extends TagSupport {

    private static Logger log = Logger.getLogger(MangoTagSupport.class);

    @Override
    public int doStartTag() throws JspException {
        try {

            MangoDispatcherHelper.post((HttpServletRequest)pageContext.getRequest(),(HttpServletResponse)pageContext.getResponse());
        } catch (ServletException | IOException e) {
            log.error(e);
        }
        return super.doStartTag();
    }
}
