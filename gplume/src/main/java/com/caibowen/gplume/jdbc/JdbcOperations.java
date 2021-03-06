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

import com.caibowen.gplume.jdbc.mapper.RowMapping;
import com.caibowen.gplume.jdbc.transaction.TransactionCallback;
import com.caibowen.gplume.jdbc.transaction.TransactionConfig;

import java.util.List;
import java.util.Map;

/**
 *
 * Supported JDBC operrations
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
public interface JdbcOperations {

    <T> T execute(TransactionConfig cfg, TransactionCallback<T> operations); //
    <T> T execute(TransactionCallback<T> operations);


    boolean execute(StatementCreator creator);// 1
    int[] batchExecute(StatementCreator creator);// 2

	boolean execute(String sql);
    boolean execute(String sql, Object... params);

    <T> T insert(StatementCreator creator, String[] cols, RowMapping<T> resultExtract);// 3

	Map<String, Object> insert(String sql, String[] cols);
	Map<String, Object> insert(String sql, String[] cols, Object... params);

//    List<Map<String, Object>> batchInsert(String[] sqls, String[] cols);//
    <T> List<T> batchInsert(StatementCreator creator, String cols[], RowMapping<T> extractor);// 4

    List<Map<String, Object>> batchInsert(StatementCreator creator, String[] cols);


    <T> T queryObject(StatementCreator psc, RowMapping<T> mapper); // 5
	<T> T queryObject(String sql, Class<T> type);
	<T> T queryObject(String sql, Class<T> type, Object... params);
	<T> T queryObject(StatementCreator psc, Class<T> type);
	
	<T> T queryObject(String sql, RowMapping<T> mapper);
	<T> T queryObject(String sql, RowMapping<T> mapper, Object... params);

    <T> List<T> queryList(StatementCreator psc, RowMapping<T> mapper);// 6

	<T> List<T> queryList(String sql, Class<T> type);
	<T> List<T> queryList(String sql, Class<T> type, Object... params);
	<T> List<T> queryList(StatementCreator psc, Class<T> type);
	
	<T> List<T> queryList(String sql, RowMapping<T> mapper);
	<T> List<T> queryList(String sql, RowMapping<T> mapper, Object... params);

    List<Map<String, Object>> queryList(String sql);
    List<Map<String, Object>> queryList(String sql, Object... params);
    List<Map<String, Object>> queryList(StatementCreator psc);


	Map<String, Object> queryMap(String sql);
	Map<String, Object> queryMap(String sql, Object... params);
	Map<String, Object> queryMap(StatementCreator psc);
	
}