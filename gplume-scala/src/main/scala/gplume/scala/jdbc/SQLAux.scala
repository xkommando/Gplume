package gplume.scala.jdbc

import java.math.BigDecimal
import java.sql._


/**
 * Created by Bowen Cai on 12/27/2014.
 */
object SQLAux {

  private case object PaddingParam

  def quoteTo(param: String)(implicit b: StringBuilder): StringBuilder = {
    b append '\''
    for (c <- param) c match {
      case '\b' => b append '\\' append 'b'
      case '\f' => b append '\\' append 'f'
      case '\n' => b append '\\' append 'n'
      case '\r' => b append '\\' append 'r'
      case '\t' => b append '\\' append 't'
      case '\\' => b append '\\' append '\\'
      case '"' => b append '\\' append '"'
      case '\'' => b append '\\' append '\''
      case '\032' => b append '\\' append 'Z'
      case o => b append o
    }
    b append '\''
  }

  implicit class SQLInterpolation(val s: StringContext) extends AnyVal {

    def sql(params: Any*): SQLOperation = {
      val sq = params.toSeq
      new SQLOperation(buildQuery(sq), sq)
    }

    private def buildQuery(params: Seq[Any]): String = {
      s.parts.zipAll(params, "", PaddingParam).foldLeft(new StringBuilder(128))(
        (sb, t) => {
          sb ++= t._1
          addPlaceholders(sb, t._2)
        }).toString
    }

    private def addPlaceholders(sb: StringBuilder, param: Any): StringBuilder = param match {
      // to fix issue #215 due to unexpected Stream#addString behavior
      case s: Stream[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      // Need to convert a Set to a List before mapping to "?", otherwise we end up with a 1-element Set
      case s: Set[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      case t: Traversable[_] => for (i <- 1 until t.size) sb.append('?').append(',')
        sb += '?'
      case `PaddingParam` => sb
      case op: SQLOperation => sb ++= op.stmt
      case _ => sb += '?'
    }

  }

  def lookupColumnName(i: Int)(implicit rsmd: ResultSetMetaData): String = {
    val name = rsmd.getColumnLabel(i)
    if (name == null || name.isEmpty)
      rsmd.getCatalogName(i)
    else
      name
  }

  def getResultSetValue(index: Int)(implicit rs: ResultSet): AnyRef = {
    val obj = rs.getObject(index)
    if (obj == null)
      return None
    val className = obj.getClass.getName
    obj match {
      case blob: Blob => blob.getBytes(1, blob.length.toInt)
      case clob: Clob => clob.getSubString(1, clob.length.toInt)
      case _ if className eq "oracle.sql.TIMESTAMP" => rs.getTimestamp(index)
      case _ if className eq "oracle.sql.TIMESTAMPTZ" => rs.getTimestamp(index)
      case _ if className.startsWith("oracle.sql.DATE") =>
        val metaDataClassName = rs.getMetaData.getColumnClassName(index)
        if (("java.sql.Timestamp" == metaDataClassName) || ("oracle.sql.TIMESTAMP" == metaDataClassName))
          rs.getTimestamp(index)
        else rs.getDate(index)
      case d: Date if "java.sql.Timestamp" == rs.getMetaData.getColumnClassName(index) => rs.getDate(index)
    }
  }
//
//  def getResultSetValue(index: Int, requiredType: Class[_])(implicit rs: ResultSet): Option[Any] = {
//    import scala.reflect.runtime.universe._
//    typeOf
//    val obj = requiredType match {
//      case s:String => rs.getString(index)
//      case Boolean => rs.getBoolean(index)
//      case Byte => rs.getByte(index)
//      case Short => rs.getShort(index)
//      case Int => rs.getInt(index)
//      case Long => rs.getLong(index)
//      case Float => rs.getFloat(index)
//      case Double => rs.getDouble(index)
//      case BigDecimal => rs.getBigDecimal(index)
//      case Date => rs.getDate(index)
//      case Timestamp => rs.getTimestamp(index)
//      case Time => rs.getTime(index)
////      case Array[Byte] => rs.getBytes(index)
//      case Blob => rs.getBlob(index)
//      case Clob => rs.getClob(index)
//      case _ => rs.getObject(index, requiredType).asInstanceOf[AnyRef]
//    }
//    if (rs.wasNull()) None else Some(obj)
//  }
}

//spring jdbc support JdbcUtils => getResultSetValue
//    if (obj.isInstanceOf[Blob]) {
//      val blob = obj.asInstanceOf[Blob]
//      return blob.getBytes(1, blob.length.toInt)
//    }
//    else if (obj.isInstanceOf[Clob]) {
//      val clob = obj.asInstanceOf[Clob]
//      return clob.getSubString(1, clob.length.toInt)
//    }
//    else if (("oracle.sql.TIMESTAMP" == className) || ("oracle.sql.TIMESTAMPTZ" == className)) {
//      return rs.getTimestamp(index)
//    }
//    else if (className != null && className.startsWith("oracle.sql.DATE")) {
//      val metaDataClassName = rs.getMetaData.getColumnClassName(index)
//      return if (("java.sql.Timestamp" == metaDataClassName) || ("oracle.sql.TIMESTAMP" == metaDataClassName))
//        rs.getTimestamp(index)
//      else rs.getDate(index)
//    }
//    else if (obj.isInstanceOf[Date]) {
//      if ("java.sql.Timestamp" == rs.getMetaData.getColumnClassName(index)) {
//        return rs.getTimestamp(index)
//      }
//    }
//    return obj