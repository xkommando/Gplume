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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

/**
 *
 * @author BowenCai
 *
 * @since 2013-5-1
 */
public class JdbcUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcUtil.class);
    /**
     * it is guaranteed that the connection returned is held by the connectionHolder on the top of the list
     * @param dataSource
     * @return
     * @throws java.sql.SQLException
     */
    @Nonnull
    public static Connection acquireConnection(@Nonnull DataSource dataSource) throws SQLException{
        ConnectionHolder holder = LocalList.last();
        if (holder != null) {
            if (holder.currentCon == null) {
                LOG.debug("Fetching JDBC Connection from DataSource");
                holder.currentCon = dataSource.getConnection();
            }
        } else {
            LOG.debug("Fetching JDBC Connection from DataSource");
            holder = new ConnectionHolder(dataSource.getConnection());
            LocalList.push(holder);
        }
        holder.addRef();
        return holder.currentCon;
    }

    public static void releaseConnection(@Nullable Connection con) throws SQLException {
        if (con == null)
            return;
        ConnectionHolder holder = LocalList.last();
        if (holder != null && (con == holder.currentCon || con.equals(holder.currentCon))) {
            holder.deRef();
            return;
        }
        con.close();
    }


	public static void closeStatement(@Nullable Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw new JdbcException("Could not close statement", e);
			}
		}
	}


	public static void closeResultSet(@Nullable ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new JdbcException("Could not close result set", e);
			}
		}
	}


	public static boolean supportsBatchUpdates(@Nonnull Connection con) {
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			if (dbmd != null) {
				return dbmd.supportsBatchUpdates();
			}
		}
		catch (SQLException e) {
			throw new JdbcException(e);
		}
		return false;
	}

	public static String lookupColumnName(@Nonnull ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

	public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			Blob blob = (Blob) obj;
			obj = blob.getBytes(1, (int) blob.length());
		}
		else if (obj instanceof Clob) {
			Clob clob = (Clob) obj;
			obj = clob.getSubString(1, (int) clob.length());
		}
		else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
			obj = rs.getTimestamp(index);
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

	public static Object getResultSetValue(@Nonnull ResultSet rs,
										   int index,
										   @Nullable Class requiredType) throws SQLException {

		if (requiredType == null) {
			return getResultSetValue(rs, index);
		}

		Object value;

		// Explicitly extract typed value, as far as possible.
		if (String.class.equals(requiredType)) {
			return rs.getString(index);
		}
		else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
			value = rs.getBoolean(index);
		}
		else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
			value = rs.getByte(index);
		}
		else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
			value = rs.getShort(index);
		}
		else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
			value = rs.getInt(index);
		}
		else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
			value = rs.getLong(index);
		}
		else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
			value = rs.getFloat(index);
		}
		else if (double.class.equals(requiredType) || Double.class.equals(requiredType) ||
				Number.class.equals(requiredType)) {
			value = rs.getDouble(index);
		}
		else if (BigDecimal.class.equals(requiredType)) {
			return rs.getBigDecimal(index);
		}
		else if (java.sql.Date.class.equals(requiredType)) {
			return rs.getDate(index);
		}
		else if (java.sql.Time.class.equals(requiredType)) {
			return rs.getTime(index);
		}
		else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
			return rs.getTimestamp(index);
		}
		else if (byte[].class.equals(requiredType)) {
			return rs.getBytes(index);
		}
		else if (Blob.class.equals(requiredType)) {
			return rs.getBlob(index);
		}
		else if (Clob.class.equals(requiredType)) {
			return rs.getClob(index);
		}
		else {
			// Some unknown type desired -> rely on getObject.
				try {
					return rs.getObject(index, requiredType);
				}
				catch (AbstractMethodError err) {
					LOG.debug("JDBC driver does not implement JDBC 4.1 'getObject(int, Class)' method", err);
				}
				catch (SQLFeatureNotSupportedException ex) {
					LOG.debug("JDBC driver does not support JDBC 4.1 'getObject(int, Class)' method", ex);
				}
				catch (SQLException ex) {
					LOG.debug("JDBC driver has limited support for JDBC 4.1 'getObject(int, Class)' method", ex);
				}

			// Fall back to getObject without type specification...
			return getResultSetValue(rs, index);
		}

		// Perform was-null check if necessary (for results that the JDBC driver returns as primitives).
		return (rs.wasNull() ? null : value);
	}
}
