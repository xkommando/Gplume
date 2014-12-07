package com.caibowen.gplume.jdbc.transaction;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * @author BowenCai
 * @since 31-10-2014.
 */
public interface TransactionManager {

    void setDataSource(DataSource dataSource);

    Transaction begin();

    Transaction begin(@Nonnull TransactionConfig config);

    void commit(@Nonnull Transaction tnx);

    void rollback(@Nonnull Transaction tnx);
}
