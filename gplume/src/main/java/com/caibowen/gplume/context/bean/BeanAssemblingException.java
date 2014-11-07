package com.caibowen.gplume.context.bean;

import java.text.MessageFormat;

/**
 * @author BowenCai
 * @since 7-11-2014.
 */
public class BeanAssemblingException extends RuntimeException {
    public BeanAssemblingException() {
    }


    public BeanAssemblingException(String message) {
        super(message);
    }

    public BeanAssemblingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanAssemblingException(String message, Object...args) {
        super(MessageFormat.format(message, args));
    }

    public BeanAssemblingException(Throwable cause) {
        super(cause);
    }

    protected BeanAssemblingException(String message, Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
