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
import com.caibowen.gplume.jdbc.mapper.SingleColumMapper;
import com.caibowen.gplume.jdbc.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public JdbcSupport() {
    }

    public JdbcSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

//-----------------------------------------------------------------------------

    private DataSource dataSource;
    private int queryTimeout;
    private int maxRow = 0;
    private int fetchSize = 0;

    private JdbcTransactionManager transactionManager = new JdbcTransactionManager();

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
        JdbcTransaction tnx = transactionManager.begin(cfg);
        T ret = null;
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


    private void configStatement(Statement st) throws SQLException{
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

    public void closeStmt(Statement stmt) {
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
            checkWarnings(ps);
            List<T> ret = null;
            if (cols != null) {
                rs = ps.getGeneratedKeys();
                ret = new ArrayList<>(8);
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
    public <T> T queryForObject(StatementCreator psc, RowMapping<T> mapper) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = psc.createStatement(connection);
            configStatement(ps);
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
    public <T> List<T> queryForList(StatementCreator psc, RowMapping<T> mapper) {
        Connection connection = acquireConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = acquireConnection();
            ps = psc.createStatement(connection);
            configStatement(ps);
            rs = ps.executeQuery();
            checkWarnings(ps);
            List<T> ls = new ArrayList<>(8);
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
    public <T> T queryForObject(final String sql, Class<T> type) {
        return queryForObject(getStatementCreator(sql), type);
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> type, Object... params) {
        return queryForObject(getStatementCreator(sql, params), type);
    }


    /**
     * get single object of requested type
     */
    @Override
    public <T> T queryForObject(StatementCreator psc, Class<T> type) {
        return queryForObject(psc, new SingleColumMapper<>(type));
    }

//-----------------------------------------------
//	get object by RowMapper
//-----------------------------------------------

    @Override
    public <T> T queryForObject(final String sql, RowMapping<T> mapper) {
        return queryForObject(getStatementCreator(sql), mapper);
    }

    @Override
    public <T> T queryForObject(final String sql,
                                RowMapping<T> mapper,
                                final Object... params) {

        return queryForObject(getStatementCreator(sql, params), mapper);
    }

    @Override
    public <T> List<T> queryForList(final String sql, RowMapping<T> mapper) {

        return queryForList(getStatementCreator(sql), mapper);
    }

    @Override
    public <T> List<T> queryForList(final String sql,
                                    RowMapping<T> mapper,
                                    final Object... params) {

        return queryForList(getStatementCreator(sql, params), mapper);
    }



//-----------------------------------------------
//	get object by type
//-----------------------------------------------

    @Override
    public <T> List<T> queryForList(final String sql, Class<T> type) {
        return queryForList(getStatementCreator(sql), type);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> type, Object... params) {
        return queryForList(getStatementCreator(sql, params), type);
    }


    @Override
    public <T> List<T> queryForList(StatementCreator psc, Class<T> type) {
        return queryForList(psc, new SingleColumMapper<>(type));
    }


//-----------------------------------------------------------------------------
//						Map
//-----------------------------------------------------------------------------

    public static final RowMapping<Map<String, Object>> COL_MAPPER = new ColumnMapper();

    @Override
    public List<Map<String, Object>> queryForList(final String sql) {
        return queryForList(getStatementCreator(sql), COL_MAPPER);
    }
    @Override
    public List<Map<String, Object>> queryForList(final String sql, Object... params) {
        return queryForList(getStatementCreator(sql, params), COL_MAPPER);
    }
    @Override
    public List<Map<String, Object>> queryForList(final StatementCreator psc) {
        return queryForList(psc, COL_MAPPER);
    }

    @Override
    public Map<String, Object> queryForMap(String sql) {
        return queryForObject(sql, COL_MAPPER);
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... params) {
        return queryForObject(sql, COL_MAPPER, params);
    }


    @Override
    public Map<String, Object> queryForMap(StatementCreator psc) {
        return queryForObject(psc, COL_MAPPER);
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


