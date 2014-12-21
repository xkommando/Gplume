/*******************************************************************************
 * Copyright (c) 2013 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.jdbc;

import com.caibowen.gplume.jdbc.mapper.ColumnMapper;
import com.caibowen.gplume.jdbc.mapper.MapExtractor;
import com.caibowen.gplume.jdbc.mapper.RowMapping;
import com.caibowen.gplume.jdbc.mapper.SingleColumnMapper;
import com.caibowen.gplume.jdbc.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  JDBC auxiliaries, automatic manage transactions, resources and exceptions.
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
public class JdbcSupport implements JdbcOperations, TransactionSupport {

    private final Logger LOG = LoggerFactory.getLogger(JdbcSupport.class);

    private boolean traceSQL;

    private DataSource dataSource;
    private int queryTimeout;
    private int maxRow;
    private int fetchSize;

    private JdbcTransactionManager transactionManager;

    public JdbcSupport() {
        this(null);
    }

    public JdbcSupport(DataSource dataSource) {
        this.dataSource = dataSource;
        transactionManager = new JdbcTransactionManager();
        transactionManager.setDataSource(dataSource);
        traceSQL = LOG.isTraceEnabled();
        maxRow = 0;
        fetchSize = 0;
    }

//-----------------------------------------------------------------------------

    public int getFetchSize() {
        return fetchSize;
    }
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
    public int getMaxRow() {
        return maxRow;
    }
    public void setMaxRow(int maxRow) {
        this.maxRow = maxRow;
    }
    public int getQueryTimeout() {
        return queryTimeout;
    }
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
    public boolean isTraceSQL() {
        return traceSQL;
    }
    public void setTraceSQL(boolean traceSQL) {
        this.traceSQL = this.traceSQL && traceSQL;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        transactionManager.setDataSource(dataSource);
    }


    @Override
    public <T> T execute(TransactionCallback<T> operations) {
        return execute(TransactionConfig.DEFAULT, operations);
    }

    @Override
    public <T> T execute(TransactionConfig cfg, TransactionCallback<T> operations) {
        Transaction tnx = transactionManager.begin(cfg);
        T ret;
        try {
            ret = operations.withTransaction(tnx);
        } catch (SQLException se) {
            if (LOG.isDebugEnabled())
                LOG.debug("Initiating transaction rollback {} on SQLException", tnx.toString(), se);
            transactionManager.rollback(tnx);
            throw new JdbcException(se);
        } catch (Exception e) {if (LOG.isDebugEnabled())
            LOG.debug("Initiating transaction rollback {} on exception", tnx.toString(), e);
            transactionManager.rollback(tnx);
            throw new JdbcException(e);
        }
        transactionManager.commit(tnx);
        return ret;
    }


    private void configStatement(Statement st) throws SQLException {
        if (maxRow > 0)
            st.setMaxRows(maxRow);
        if (fetchSize > 0)
            st.setFetchSize(fetchSize);
        ConnectionHolder holder = LocalList.last();
        int timeout = TransactionConfig.DEFAULT_TIMEOUT;
        if (holder != null && holder.queryTimeout != timeout)
            timeout = holder.queryTimeout;
        else if (queryTimeout != timeout)
            timeout = queryTimeout;
        st.setQueryTimeout(timeout);
    }


    public Connection acquireConnection() {
        try {
            return JdbcUtil.acquireConnection(dataSource);
        } catch (SQLException e) {
            throw new JdbcException("Could not get JDBC Connection", e);
        }
    }

    public void releaseConnection(Connection con) {
        try {
            JdbcUtil.releaseConnection(con);
        } catch (SQLException ex) {
            LOG.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            LOG.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    private void checkWarnings(Statement st) throws SQLException {
        if (LOG.isDebugEnabled()) {
            SQLWarning warningToLog = st.getWarnings();
            while (warningToLog != null) {
                LOG.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState()
                        + "', error code '" + warningToLog.getErrorCode()
                        + "', message [" + warningToLog.getMessage() + "]");
                warningToLog = warningToLog.getNextWarning();
            }
        }
    }

    protected void closeStmt(Statement stmt) {
        if (stmt == null)
            return;
        try {
            stmt.close();
        } catch (SQLException e) {
            LOG.trace("Could not close JDBC Statement", e);
        } catch (Throwable e) {
            LOG.trace("Unexpected exception on closing JDBC Statement", e);
        }
    }


//-----------------------------------------------------------------------------
//						execute
//-----------------------------------------------------------------------------

    /**
     * the base function
     */
    @Override
    public boolean execute(StatementCreator psc) {

        PreparedStatement st = null;
        Connection connection = acquireConnection();
        try {
            st = psc.createStatement(connection);
            configStatement(st);
            if (traceSQL)
                LOG.trace("Executing prepared SQL statement[" + st + "]");
            boolean noRs = st.execute();
            checkWarnings(st);
            if (noRs)
                return st.getUpdateCount() > 0;
            else
                return true;
        } catch (SQLException e) {
            closeStmt(st);
            releaseConnection(connection);
            throw new JdbcException(e);
        } finally {
            closeStmt(st);
            releaseConnection(connection);
        }
    }

    @Override
    public int[] batchExecute(StatementCreator creator) {
        Connection connection = acquireConnection();
        Statement st = null;
        try {
            st = creator.createStatement(connection);
            configStatement(st);
            if (traceSQL)
                LOG.trace("Batch executing prepared SQL statement[" + st + "]");
            int[] out = st.executeBatch();
            checkWarnings(st);
            return out;
        } catch (SQLException e) {
            JdbcUtil.closeStatement(st);
            releaseConnection(connection);
            throw new JdbcException(e);
        } finally {
            JdbcUtil.closeStatement(st);
            releaseConnection(connection);
        }
    }


    @Override
    public <T> T insert(StatementCreator psc, final String[] cols, RowMapping<T> resultExtract) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = psc.createStatement(connection);
            configStatement(ps);
            if (traceSQL)
                LOG.trace("Executing prepared SQL statement[" + ps + "]");
            ps.execute();
            checkWarnings(ps);
            T ret = null;
            if (cols != null) {
                rs = ps.getGeneratedKeys();
                if (rs.next())
                    ret = resultExtract.extract(rs);
            }
            return ret;
        } catch (SQLException e) {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
            throw new JdbcException(e);
        } finally {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
        }
    }

    @Override
    public  <T> List<T> batchInsert(StatementCreator creator, final String cols[], RowMapping<T> extractor) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = creator.createStatement(connection);
            ps.executeBatch();
            if (traceSQL)
                LOG.trace("Batch executing prepared SQL statement[" + ps + "]");
            checkWarnings(ps);
            List<T> ret = null;
            if (cols != null) {
                rs = ps.getGeneratedKeys();
                ret = new ArrayList<>(16);
                while (rs.next())
                    ret.add(extractor.extract(rs));
            }
            return ret;
        } catch (SQLException e) {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
            throw new JdbcException(e.getSQLState(),e);
        } finally {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
        }
    }

    @Override
    public <T> T queryObject(StatementCreator psc, RowMapping<T> mapper) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = psc.createStatement(connection);
            configStatement(ps);
            if (traceSQL)
                LOG.trace("Executing prepared SQL statement[" + ps + "]");
            rs = ps.executeQuery();
            checkWarnings(ps);
            T o = null;
            if (rs.next()) {
                o = mapper.extract(rs);
                assert (rs.isLast());
            }
            return o;
        } catch (SQLException e) {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
            throw new JdbcException(e.getSQLState(),e);
        } finally {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
        }
    }

    @Override
    public <T> List<T> queryList(StatementCreator psc, RowMapping<T> mapper) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = acquireConnection();
            ps = psc.createStatement(connection);
            configStatement(ps);
            if (traceSQL)
                LOG.trace("Executing prepared SQL statement[" + ps + "]");
            rs = ps.executeQuery();
            checkWarnings(ps);
            List<T> ls = new ArrayList<>(32);
            while (rs.next()) {
                ls.add(mapper.extract(rs));
            }
            return ls;
        } catch (SQLException e) {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
            throw new JdbcException(e.getSQLState(),e);
        } finally {
            JdbcUtil.closeResultSet(rs);
            JdbcUtil.closeStatement(ps);
            releaseConnection(connection);
        }
    }

    /**
     * @return Map<String, T> is the getGenerated keys
     *
     *  example:
     *
     *  keys =insertAll(entity, new String[]{"id", "time_created"})
     *
     *  entityId = keys.get("id");
     *
     */
    @Override
    public Map<String, Object> insert(final String sql, final String[] cols) {

        return insert(new StatementCreator() {
            @Nonnull
            @Override
            public PreparedStatement createStatement(Connection con)
                    throws SQLException {
                return cols == null ? con.prepareStatement(sql)
                        : con.prepareStatement(sql, cols);
            }
        }, cols, new MapExtractor(cols));
    }

    @Override
    public Map<String, Object> insert(final String sql,
                                      final String[] cols,
                                      final Object... params) {
        return insert(new StatementCreator() {
            @Nonnull
            @Override
            public PreparedStatement createStatement(Connection con)
                    throws SQLException {
                PreparedStatement ps =con.prepareStatement(sql, cols);
                setParams(ps, params);
                return ps;}
        }, cols, new MapExtractor(cols));
    }

    @Override
    public List<Map<String, Object>> batchInsert(StatementCreator creator, String[] cols) {
        return batchInsert(creator, cols, COL_MAPPER);
    }

    @Override
    public boolean execute(final String sql) {
        return execute(getStatementCreator(sql));
    }

    @Override
    public boolean execute(final String sql, final Object... params) {
        return execute(getStatementCreator(sql, params));
    }


    /**
     * get object of requested type
     */
    @Override
    public <T> T queryObject(final String sql, Class<T> type) {
        return queryObject(getStatementCreator(sql), type);
    }

    @Override
    public <T> T queryObject(String sql, Class<T> type, Object... params) {
        return queryObject(getStatementCreator(sql, params), type);
    }


    /**
     * get single object of requested type
     */
    @Override
    public <T> T queryObject(StatementCreator psc, Class<T> type) {
        return queryObject(psc, new SingleColumnMapper<>(type));
    }

//-----------------------------------------------
//	get object by RowMapper
//-----------------------------------------------

    @Override
    public <T> T queryObject(final String sql, RowMapping<T> mapper) {
        return queryObject(getStatementCreator(sql), mapper);
    }

    @Override
    public <T> T queryObject(final String sql,
                                RowMapping<T> mapper,
                                final Object... params) {

        return queryObject(getStatementCreator(sql, params), mapper);
    }

    @Override
    public <T> List<T> queryList(final String sql, RowMapping<T> mapper) {

        return queryList(getStatementCreator(sql), mapper);
    }

    @Override
    public <T> List<T> queryList(final String sql,
                                    RowMapping<T> mapper,
                                    final Object... params) {

        return queryList(getStatementCreator(sql, params), mapper);
    }



//-----------------------------------------------
//	get object by type
//-----------------------------------------------

    @Override
    public <T> List<T> queryList(final String sql, Class<T> type) {
        return queryList(getStatementCreator(sql), type);
    }

    @Override
    public <T> List<T> queryList(String sql, Class<T> type, Object... params) {
        return queryList(getStatementCreator(sql, params), type);
    }


    @Override
    public <T> List<T> queryList(StatementCreator psc, Class<T> type) {
        return queryList(psc, new SingleColumnMapper<>(type));
    }


//-----------------------------------------------------------------------------
//						Map
//-----------------------------------------------------------------------------

    public static final RowMapping<Map<String, Object>> COL_MAPPER = new ColumnMapper();

    @Override
    public List<Map<String, Object>> queryList(final String sql) {
        return queryList(getStatementCreator(sql), COL_MAPPER);
    }
    @Override
    public List<Map<String, Object>> queryList(final String sql, Object... params) {
        return queryList(getStatementCreator(sql, params), COL_MAPPER);
    }
    @Override
    public List<Map<String, Object>> queryList(final StatementCreator psc) {
        return queryList(psc, COL_MAPPER);
    }

    @Override
    public Map<String, Object> queryMap(String sql) {
        return queryObject(sql, COL_MAPPER);
    }

    @Override
    public Map<String, Object> queryMap(String sql, Object... params) {
        return queryObject(sql, COL_MAPPER, params);
    }


    @Override
    public Map<String, Object> queryMap(StatementCreator psc) {
        return queryObject(psc, COL_MAPPER);
    }


    public static void setParams(PreparedStatement ps, Object... params) throws SQLException {

        if (ps == null || params == null)
            return;

        for (int i = 0; i != params.length; ++i) {
            Object val = params[i];
            if (val != null)
                ps.setObject(i + 1, params[i]);
            else {
                int type = ps.getParameterMetaData().getParameterType(i + i);
                ps.setNull(i + 1, type);
            }
        }
    }

    /**
     * create a StatementCreator out of a String
     *
     * @param sql
     * @return
     */
    protected static StatementCreator getStatementCreator(final String sql) {
        return new StatementCreator() {
            @Nonnull
            @Override
            public PreparedStatement createStatement(Connection con)
                    throws SQLException {
                return con.prepareStatement(sql);
            }
        };
    }

    /**
     * create a StatementCreator out of a String and its parameters
     *
     * @param sql
     * @param params
     * @return
     */
    protected static StatementCreator getStatementCreator(final String sql, final Object... params) {
        return new StatementCreator() {

            @Nonnull
            @Override
            public PreparedStatement createStatement(Connection con)
                    throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql);
                setParams(ps, params);
                return ps;
            }
        };
    }


}


