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
package com.caibowen.gplume.jdbc.mapper;
import com.caibowen.gplume.common.collection.NoCaseMap;
import com.caibowen.gplume.jdbc.JdbcUtil;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
public class ColumnMapper implements RowMapping<Map<String, Object>> {

	@Override
	public Map<String, Object> extract(@Nonnull ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columnCount = md.getColumnCount();

		NoCaseMap<Object> m = new NoCaseMap<>(columnCount);
		
		for (int i = 1; i <= columnCount; ++i) {
			String key = JdbcUtil.lookupColumnName(md, i);
			Object var = JdbcUtil.getResultSetValue(rs, i);
			m.put(key, var);
		}
		return m;
	}

}