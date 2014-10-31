package com.caibowen.gplume.jdbc.transaction;

import javax.annotation.Nonnull;

/**
 * @author BowenCai
 * @since 31-10-2014.
 */
public interface TransactionManager {
    JdbcTransaction begin();

    JdbcTransaction begin(@Nonnull TransactionConfig config);

    void commit(@Nonnull Transaction tnx);

    void rollback(@Nonnull Transaction tnx);
}
