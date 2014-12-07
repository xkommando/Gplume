package com.caibowen.gplume.jdbc.transaction;

import com.caibowen.gplume.annotation.Functional;

import javax.annotation.Nonnull;

/**
 * @author BowenCai
 * @since 28-10-2014.
 */
@Functional
public interface TransactionCallback<T> {

    T withTransaction(@Nonnull Transaction transaction) throws Exception;

}
