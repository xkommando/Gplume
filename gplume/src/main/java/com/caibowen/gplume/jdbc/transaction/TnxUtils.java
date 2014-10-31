package com.caibowen.gplume.jdbc.transaction;

import com.caibowen.gplume.annotation.Internal;
import com.caibowen.gplume.jdbc.ConnectionHolder;
import com.caibowen.gplume.jdbc.JdbcException;
import com.caibowen.gplume.jdbc.LocalList;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author BowenCai
 * @since 29-10-2014.
 */
@Internal
class TnxUtils {

    @Nonnull
    static ConnectionHolder getHolderForTnx(@Nonnull DataSource dataSource) {
        ConnectionHolder holder = LocalList.last();
        if (holder == null || holder.tnxActive) {
            try {
                holder = new ConnectionHolder(dataSource.getConnection());
            } catch (SQLException se) {
                throw new JdbcException("Could not get connection for transaction", se);
            }
            LocalList.push(holder);
        } else if (holder.currentCon == null) {
            try {
                holder = new ConnectionHolder(dataSource.getConnection());
            } catch (SQLException se) {
                throw new JdbcException("Could not get connection for transaction", se);
            }
        }
//        holder.addRef(); // not yet required
        holder.tnxActive = true;
        return holder;
    }

    static void prepareForTnx(TransactionConfig config, JdbcTransaction tnx) {
        ConnectionHolder holder = tnx.holder;
        Connection _con = holder.currentCon;
        try {
            _con.setReadOnly(config.isReadOnly());
            int _cfgIso = config.getIsolationLevel();
            if (_cfgIso != TransactionConfig.DEFAULT_ISOLATION) {
                int oldLevel = _con.getTransactionIsolation();
                if (oldLevel != config.getIsolationLevel()) {
                    _con.setTransactionIsolation(config.getIsolationLevel());
                    tnx.prevISOLevel = oldLevel;
                }
            }
            if (_con.getAutoCommit()) {
                _con.setAutoCommit(false);
                if (config.isResetReadOnly())
                    tnx.resetAutoCommit = true;
            }
        } catch (SQLException se) {
            throw new JdbcException("Could not config connection for transaction", se);
        }

        if (config.getTimeout() != TransactionConfig.DEFAULT_TIMEOUT)
            holder.queryTimeout = config.getTimeout();
    }


    static void restoreConnection(JdbcTransaction tnx) throws SQLException {
        Connection con = tnx.holder.currentCon;
        int _prevIso = tnx.prevISOLevel;
        if (_prevIso != TransactionConfig.DEFAULT_ISOLATION
                && _prevIso != con.getTransactionIsolation())
            con.setTransactionIsolation(_prevIso);

        if (tnx.resetAutoCommit)
            con.setAutoCommit(true);

        if (con.isReadOnly())
            con.setReadOnly(false);
    }

}
