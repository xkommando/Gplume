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

import com.caibowen.gplume.jdbc.JdbcUtil;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
public class SingleColumnMapper<T> implements RowMapping<T> {

	Class<T> type;
	
	public SingleColumnMapper(Class<T> type) {
		this.type = type;
	}

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}

	@Override
	public T extract(@Nonnull ResultSet rs) throws SQLException {

		T o = (T) JdbcUtil.getResultSetValue(rs, 1, type);
		if (o != null && type.isInstance(o)) {
			return o;
		} else {
			throw new RuntimeException(
					"failed to get requested value: type mismatch");
		}
	}

}




