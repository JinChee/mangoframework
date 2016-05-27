package org.mangoframework.core.exception;

import org.mangoframework.core.dispatcher.Parameter;
import org.mangoframework.core.utils.ResultviewUtils;

/**
 * User: zhoujingjie
 * Date: 16/4/22
 * Time: 22:01
 */
public class SimpleExceptionHandler implements ExceptionHandler{

    @Override
    public void process(Parameter parameter,Exception e) {
        try {
            ResultviewUtils.getResultView(parameter.getExtension()).handleException(parameter,e);
        } catch (ClassNotFoundException |IllegalAccessException |InstantiationException e1 ) {
            parameter.getResponse().setStatus(503);
            e1.printStackTrace();
        }
    }
}
