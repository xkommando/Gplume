package com.caibowen.gplume.jdbc.transaction;

import com.caibowen.gplume.jdbc.ConnectionHolder;
import com.caibowen.gplume.jdbc.JdbcException;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author BowenCai
 * @since 28-10-2014.
 */
public class JdbcTransaction implements Transaction {

    boolean completed = false;
    boolean rollbackOnly = false;

    Savepoint savepoint;

    ConnectionHolder holder;

    int prevISOLevel = TransactionConfig.DEFAULT_ISOLATION;
    boolean resetAutoCommit = true;

    private int savepointCount = 0;

    @Override
    public Savepoint createSavepoint() {
        savepointCount += 1;
        return createSavepoint("__SAVEPOINT__" + savepoint);
    }

    @Override
    @Nonnull
    public Savepoint createSavepoint(@Nonnull String name) {
        try {
            return holder.currentCon.setSavepoint(name);
        } catch (SQLException e) {
            throw new JdbcException("Could not create JDBC savepoint", e);
        }
    }

    @Override
    public boolean hasSavepoint() {
        return savepoint != null;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * This instructs the transaction manager
     * that the only possible outcome of the transaction may be a rollback, as
     * alternative to throwing an exception which would in turn trigger a rollback.
     *
     * @param rollbackOnly
     */
    @Override
    public void setRollbackOnly(boolean rollbackOnly) {
        this.rollbackOnly = rollbackOnly;
    }

    void rollbackToSavepoint() {
        rollback(this.savepoint);
    }

    @Override
    public void rollback(@Nonnull Savepoint sp) {
        try {
            holder.currentCon.rollback(sp);
        } catch (SQLException e) {
            throw new JdbcException("Could not roll back to JDBC savepoint", e);
        }

    }

    void rollback() {
        try {
            holder.currentCon.rollback();
        } catch (SQLException e) {
            throw new JdbcException("Could not roll back to JDBC savepoint", e);
        }
    }

    void releaseSavepoint() {
        releaseSavepoint(this.savepoint);
    }

    void releaseSavepoint(@Nonnull Savepoint sp) {
        try {
            holder.currentCon.releaseSavepoint(sp);
        } catch (SQLException e) {
            throw new JdbcException("Could not explicitly release JDBC savepoint", e);
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "completed=" + completed +
                ", rollbackOnly=" + rollbackOnly +
                ", savepoint=" + savepoint +
                ", holder=" + holder +
                ", prevISOLevel=" + prevISOLevel +
                ", resetAutoCommit=" + resetAutoCommit +
                ", savepointCount=" + savepointCount +
                '}';
    }
}
