package com.caibowen.gplume.jdbc;

/**
 *  unchecked SQL exception
 * @author BowenCai
 * @since 28-10-2014.
 */
public class JdbcException extends RuntimeException {

    public JdbcException() {
    }


    public JdbcException(String message) {
        super(message);
    }


    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }


    public JdbcException(Throwable cause) {
        super(cause);
    }

    protected JdbcException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
