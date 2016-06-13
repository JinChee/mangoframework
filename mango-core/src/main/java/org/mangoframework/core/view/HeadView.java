package org.mangoframework.core.view;

import org.mangoframework.core.dispatcher.Parameter;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhoujingjie
 * @date 2016-06-13
 */
public class HeadView extends ResultView {
    @Override
    public void doRepresent(Parameter parameter) throws Exception {
        HttpServletResponse response = parameter.getResponse();
        response.setStatus(200);
    }
}
