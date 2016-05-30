package org.mangoframework.core.view;

import org.mangoframework.core.dispatcher.Parameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author zhoujingjie
 * @date 2016/4/26
 */
public class RedirectView extends ResultView {

    private String redirectURL;

    public RedirectView(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    @Override
    public void doRepresent(Parameter parameter) throws Exception {
        parameter.getResponse().sendRedirect(redirectURL);
    }
}
