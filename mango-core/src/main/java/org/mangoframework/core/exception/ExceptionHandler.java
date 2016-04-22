package org.mangoframework.core.exception;

import org.mangoframework.core.Parameter;

/**
 * User: zhoujingjie
 * Date: 16/4/22
 * Time: 21:52
 */
public interface ExceptionHandler {

    void process(Parameter parameter,Exception e);
}
