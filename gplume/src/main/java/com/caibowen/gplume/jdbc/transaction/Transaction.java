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
public class Transaction {

    boolean completed = false;
    boolean rollbackOnly = false;

    Savepoint savepoint;

    ConnectionHolder holder;

    int prevISOLevel = TransactionConfig.DEFAULT_ISOLATION;
    boolean resetAutoCommit = true;

    private Integer savepointCount = 0;

    public Savepoint createSavepoint() {
        savepointCount += 1;
        return createSavepoint(savepoint.toString());
    }

    @Nonnull
    public Savepoint createSavepoint(@Nonnull String name) {
        try {
            return holder.currentCon.setSavepoint(name);
        } catch (SQLException e) {
            throw new JdbcException("Could not create JDBC savepoint", e);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
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
    public void setRollbackOnly(boolean rollbackOnly) {
        this.rollbackOnly = rollbackOnly;
    }

    public void rollbackToSavepoint() {
        rollback(this.savepoint);
    }

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
                "\r\ncompleted=" + completed +
                "\r\n, rollbackOnly=" + rollbackOnly +
                "\r\n, savepoint=" + savepoint +
                "\r\n, holder=" + holder +
                "\r\n, prevISOLevel=" + prevISOLevel +
                "\r\n, resetAutoCommit=" + resetAutoCommit +
                "\r\n, savepointCount=" + savepointCount +
                "\r\n}";
    }
}
