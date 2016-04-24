package org.mangoframework.core.exception;

/**
 * User: zhoujingjie
 * Date: 16/4/22
 * Time: 20:57
 */
public class ControllerNotFoundException extends MangoException {
    public ControllerNotFoundException() {
        super();    
    }

    public ControllerNotFoundException(String message) {
        super(message);   
    }

    public ControllerNotFoundException(String message, Throwable cause) {
        super(message, cause);    
    }

    public ControllerNotFoundException(Throwable cause) {
        super(cause);    
    }

    protected ControllerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);    
    }
}
