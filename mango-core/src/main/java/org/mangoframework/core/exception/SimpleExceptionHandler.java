package org.mangoframework.core.exception;

import org.mangoframework.core.dispatcher.Parameter;

/**
 * User: zhoujingjie
 * Date: 16/4/22
 * Time: 22:01
 */
public class SimpleExceptionHandler implements ExceptionHandler{

    @Override
    public void process(Parameter parameter,Exception e) {
        parameter.getResponse().setStatus(503);
        e.printStackTrace();
    }
}
