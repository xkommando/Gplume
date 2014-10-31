package com.caibowen.gplume.jdbc.transaction;

import javax.annotation.Nonnull;

/**
 * @author BowenCai
 * @since 28-10-2014.
 */
public interface TransactionCallback<T> {

    T withTransaction(@Nonnull JdbcTransaction transaction) throws Exception;

}
