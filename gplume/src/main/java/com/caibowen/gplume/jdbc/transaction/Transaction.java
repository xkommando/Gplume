package com.caibowen.gplume.jdbc.transaction;

import javax.annotation.Nonnull;
import java.sql.Savepoint;

/**
 * @author BowenCai
 * @since 31-10-2014.
 */
public interface Transaction {

    Savepoint createSavepoint();

    @Nonnull
    Savepoint createSavepoint(@Nonnull String name);

    boolean isCompleted();

    boolean isRollbackOnly();

    /**
     * This instructs the transaction manager
     * that the only possible outcome of the transaction may be a rollback, as
     * alternative to throwing an exception which would in turn trigger a rollback.
     *
     * @param rollbackOnly
     */
    void setRollbackOnly(boolean rollbackOnly);

    void rollback(@Nonnull Savepoint sp);
}
