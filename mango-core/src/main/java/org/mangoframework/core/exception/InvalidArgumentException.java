package org.mangoframework.core.exception;

/**
 * 无效的参数
 * @author zhoujingjie
 * @date 2016-05-12
 */
public class InvalidArgumentException extends MangoException {
    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(String message) {
        super(message);
    }

    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidArgumentException(Throwable cause) {
        super(cause);
    }

    protected InvalidArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
