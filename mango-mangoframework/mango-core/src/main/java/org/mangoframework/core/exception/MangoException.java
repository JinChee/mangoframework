package org.mangoframework.core.exception;

/**
 * @author zhoujingjie
 * @date 2016/4/22
 */
public class MangoException extends RuntimeException{
    public MangoException() {
        super();
    }

    public MangoException(String message) {
        super(message);
    }

    public MangoException(String message, Throwable cause) {
        super(message, cause);
    }

    public MangoException(Throwable cause) {
        super(cause);
    }

    protected MangoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
