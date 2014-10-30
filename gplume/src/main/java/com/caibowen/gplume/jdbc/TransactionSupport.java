package com.caibowen.gplume.jdbc;

import com.caibowen.gplume.jdbc.transaction.TransactionCallback;
import com.caibowen.gplume.jdbc.transaction.TransactionConfig;

import javax.sql.DataSource;

/**
 * @author BowenCai
 * @since 30-10-2014.
 */
public interface TransactionSupport {

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);

    <T> T execute(TransactionCallback<T> operations);

    <T> T execute(TransactionConfig cfg, TransactionCallback<T> operations);
}
