package com.caibowen.gplume.scala.jdbc

import java.sql.{ResultSet, Timestamp}

import com.caibowen.gplume.jdbc.mapper.{RowMapping => JMapping}

/**
 * @author BowenCai
 * @since  17/12/2014.
 */
object RowMappings {

  val BOOLEAN_MAPPING = new JMapping[Boolean] {
    def extract(rs: ResultSet) = rs.getBoolean(1)
  }


  val SHORT_MAPPING = new JMapping[Short] {
    def extract(rs: ResultSet) = rs.getShort(1)
  }

  val INT_MAPPING = new JMapping[Integer] {
    def extract(rs: ResultSet) = rs.getInt(1)
  }

  val LONG_MAPPING = new JMapping[Long] {
    def extract(rs: ResultSet) = rs.getLong(1)
  }

  val FLOAT_MAPPING = new JMapping[Float] {
    def extract(rs: ResultSet) = rs.getFloat(1)
  }

  val DOUBLE_MAPPING = new JMapping[Double] {
    def extract(rs: ResultSet) = rs.getDouble(1)
  }

  val TIMESTAMP_MAPPING = new JMapping[Timestamp] {
    def extract(rs: ResultSet) = rs.getTimestamp(1)
  }

  val STR_MAPPING = new JMapping[String] {
    def extract(rs: ResultSet) = rs.getString(1)
  }

  val OBJ_MAPPING = new JMapping[AnyRef] {
    def extract(rs: ResultSet) = rs.getObject(1)
  }
}
