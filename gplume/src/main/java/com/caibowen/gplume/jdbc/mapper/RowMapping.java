package com.caibowen.gplume.jdbc.mapper;

import com.caibowen.gplume.annotation.Functional;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author BowenCai
 *
 * @since 2013-5-6
 */
@Functional
public interface RowMapping<T> {

	T extract(@Nonnull ResultSet rs) throws SQLException;

    RowMapping<Boolean> BOOLEAN_ROW_MAPPING = new RowMapping<Boolean>() {
        @Override
        public Boolean extract(ResultSet rs) throws SQLException {
            return rs.getBoolean(1);
        }
    };
    RowMapping<Short> SHORT_ROW_MAPPING = new RowMapping<Short>() {
        @Override
        public Short extract(ResultSet rs) throws SQLException {
            return rs.getShort(1);
        }
    };

    RowMapping<Integer> INT_ROW_MAPPING = new RowMapping<Integer>() {
        @Override
        public Integer extract(ResultSet rs) throws SQLException {
            return rs.getInt(1);
        }
    };
    RowMapping<Long> LONG_ROW_MAPPING = new RowMapping<Long>() {
        @Override
        public Long extract(ResultSet rs) throws SQLException {
            return rs.getLong(1);
        }
    };
    RowMapping<Float> FLOAT_ROW_MAPPING = new RowMapping<Float>() {
        @Override
        public Float extract(ResultSet rs) throws SQLException {
            return rs.getFloat(1);
        }
    };
    RowMapping<Double> DOUBLE_ROW_MAPPING = new RowMapping<Double>() {
        @Override
        public Double extract(ResultSet rs) throws SQLException {
            return rs.getDouble(1);
        }
    };
    RowMapping<Timestamp> TIMESTAMP_ROW_MAPPING = new RowMapping<Timestamp>() {
        @Override
        public Timestamp extract(ResultSet rs) throws SQLException {
            return rs.getTimestamp(1);
        }
    };
    RowMapping<String> STR_ROW_MAPPING = new RowMapping<String>() {
        @Override
        public String extract(ResultSet rs) throws SQLException {
            return rs.getString(1);
        }
    };

    RowMapping<Object> OBJ_ROW_MAPPING = new RowMapping<Object>() {
        @Override
        public Object extract(ResultSet rs) throws SQLException {
            return rs.getObject(1);
        }
    };

}
