package com.caibowen.gplume.jdbc.transaction;

import com.caibowen.gplume.jdbc.ConnectionHolder;
import com.caibowen.gplume.jdbc.JdbcException;
import com.caibowen.gplume.jdbc.LocalList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 *
 * @author BowenCai
 * @since 28-10-2014.
 */
public class JdbcTransactionManager implements TransactionManager {

    private final Logger LOG = LoggerFactory.getLogger(JdbcTransactionManager.class);

    private DataSource dataSource;
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public JdbcTransaction begin() {
        return begin(TransactionConfig.DEFAULT);
    }

    @Override
    public JdbcTransaction begin(@Nonnull TransactionConfig config) {

        JdbcTransaction tnx = new JdbcTransaction();
        tnx.completed = false;

        tnx.holder = TnxUtils.getHolderForTnx(dataSource);

        TnxUtils.prepareForTnx(config, tnx);
        if (LOG.isDebugEnabled())
            LOG.debug("Creating new transaction: {} ", config.toString());

        return tnx;
    }


    @Override
    public void commit(@Nonnull JdbcTransaction tnx) {
        if (tnx.completed)
            throw new IllegalStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        if (tnx.isRollbackOnly()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transactional code has requested rollback");
            }
            rollback(tnx);
            return;
        }

        if (tnx.savepoint != null) {
            if (LOG.isDebugEnabled())
                LOG.debug("Releasing transaction savepoint");
            tnx.releaseSavepoint();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Committing JDBC transaction on Connection {}", tnx.holder.currentCon.toString());
        }
        try {
            tnx.holder.currentCon.commit();
        }
        catch (SQLException ex) {
            LOG.debug("rolling back transaction on application exception", ex);
            rollback(tnx);
            throw new JdbcException("Could not commit JDBC transaction", ex);
        }
        complete(tnx);
    }

    @Override
    public void rollback(@Nonnull JdbcTransaction tnx) {
        if (tnx.completed) {
            throw new IllegalStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rolling back JDBC transaction on Connection: {}", tnx.holder.currentCon.toString());
        }
        if (tnx.savepoint != null)
            tnx.rollbackToSavepoint();
        else
            tnx.rollback();

        complete(tnx);
    }


    private void complete(JdbcTransaction tnx) {

        ConnectionHolder holder = tnx.holder;
        holder.tnxActive = false;

        try {
            if (LocalList.size() > 1) {
                LocalList.remove(holder);
                holder.release();
            } else
                TnxUtils.restoreConnection(tnx);
        } catch (SQLException ex) {
            LOG.debug("Could not close JDBC Connection", ex);
        }
        catch (Throwable ex) {
            LOG.debug("Unexpected exception on closing JDBC Connection", ex);
        }

        tnx.completed = true;
    }


}
