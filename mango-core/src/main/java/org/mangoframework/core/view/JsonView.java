package org.mangoframework.core.view;

import org.mangoframework.core.Parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * dependency: com.alibaba.fastjson
 * @author : zhoujingjie
 * @Date: 16/4/22
 */
public class JsonView implements ResultView {
    @Override
    public void doRepresent(Parameter parameter,Object data) {
        HttpServletResponse response = parameter.getResponse();
        HttpServletRequest request = parameter.getRequest();
        response.setContentType("application/json");
    }
}
