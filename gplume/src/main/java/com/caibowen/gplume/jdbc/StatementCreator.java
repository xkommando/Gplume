package com.caibowen.gplume.jdbc;

import com.caibowen.gplume.annotation.Functional;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
@Functional
public interface StatementCreator {

    @Nonnull
	PreparedStatement createStatement(@Nonnull Connection con) throws SQLException;
}
