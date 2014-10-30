package com.caibowen.gplume.jdbc.transaction;

import com.caibowen.gplume.jdbc.ConnectionHolder;
import com.caibowen.gplume.jdbc.JdbcException;
import com.caibowen.gplume.jdbc.LocalList;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author BowenCai
 * @since 28-10-2014.
 */
public class TransactionManager {

    private DataSource dataSource;
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction begin() {
        return begin(TransactionConfig.DEFAULT);
    }

    public Transaction begin(@Nonnull TransactionConfig config) {

        Transaction tnx = new Transaction();
        tnx.completed = false;

        tnx.holder = TnxUtils.getHolderForTnx(dataSource);

        TnxUtils.prepareForTnx(config, tnx);
        return tnx;
    }


    public void commit(@Nonnull Transaction tnx) {
        if (tnx.completed)
            throw new IllegalStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        if (tnx.isRollbackOnly()) {
            rollback(tnx);
            return;
        }
        if (tnx.savepoint != null) {
            tnx.releaseSavepoint();
        }
        try {
            tnx.holder.currentCon.commit();
        }
        catch (SQLException ex) {
            // LOGGGG
            rollback(tnx);
            throw new JdbcException("Could not commit JDBC transaction", ex);
        }
        complete(tnx);
    }

    public void rollback(@Nonnull Transaction tnx) {
        if (tnx.completed) {
            throw new IllegalStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }
        if (tnx.savepoint != null)
            tnx.rollbackToSavepoint();
        else
            tnx.rollback();

        complete(tnx);
    }


    private void complete(Transaction tnx) {

        TnxUtils.restoreConnection(tnx);
        ConnectionHolder holder = tnx.holder;
        if (LocalList.size() > 1) {
            LocalList.remove(holder);
        }
        holder.tnxActive = false;
        try {
            holder.deRef();
        } catch (SQLException e) {
            /// LOG
            throw new JdbcException(e);
        }
        tnx.completed = true;
    }


}
