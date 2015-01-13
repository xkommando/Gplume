package gplume.scala.jdbc

import javax.annotation.Nullable

import scala.collection.{GenTraversableOnce, mutable}
import java.math.MathContext
import java.sql.{Connection, Statement, PreparedStatement, ResultSet}

import scala.reflect.ClassTag

/**
 * Created by Bowen Cai on 12/27/2014.
 */
object SQLOperation {

  def apply(value: String, parameters: Seq[Any] = Nil) = new SQLOperation(value, parameters)

  def unapply(op: SQLOperation): Option[(String, Seq[Any])] = Some((op.stmt, op.parameters))

  // single value collectors
  @inline val colBool = (rs: ResultSet) => rs.getBoolean(1)
  @inline val colByte = (rs: ResultSet) => rs.getByte(1)
  @inline val colShort = (rs: ResultSet) => rs.getShort(1)
  @inline val colInt = (rs: ResultSet) => rs.getInt(1)
  @inline val colLong = (rs: ResultSet) => rs.getLong(1)
  @inline val colFloat = (rs: ResultSet) => rs.getFloat(1)
  @inline val colDouble = (rs: ResultSet) => rs.getDouble(1)
  @inline val colBytes = (rs: ResultSet) => rs.getBytes(1)
  @inline val colStr = (rs: ResultSet) => rs.getString(1)
  @inline val colDate = (rs: ResultSet) => rs.getDate(1)
  @inline val colTime= (rs: ResultSet) => rs.getTime(1)
  @inline val colBigDecimal = (rs: ResultSet) => new BigDecimal(rs.getBigDecimal(1), MathContext.DECIMAL128)
  @inline val colObj = (rs: ResultSet) => rs.getObject(1)
  @inline val colBlob = (rs: ResultSet) => rs.getBlob(1)
  @inline val colClob = (rs: ResultSet) => rs.getClob(1)

  // no oeration
  def NOP[A]: A=>Unit = (a: A)=>{}
  //  no return
  //  def NRET[A, B]: A=>B = (a:A)=>{null.asInstanceOf[B]}

  def bind(stmt: PreparedStatement, params: GenTraversableOnce[Any]): Unit = {
    if (params != null && params.size > 0) {
      var i = 0
      for (param <- params) {
        i = i + 1
        param match {
          case null => stmt.setObject(i, null)
          case p: java.sql.Array => stmt.setArray(i, p)
          case p: BigDecimal => stmt.setBigDecimal(i, p.bigDecimal)
          case p: Boolean => stmt.setBoolean(i, p)
          case p: Byte => stmt.setByte(i, p)
          case p: Array[Byte] => stmt.setBytes(i, p)
          case p: java.sql.Date => stmt.setDate(i, p)
          case p: Double => stmt.setDouble(i, p)
          case p: Float => stmt.setFloat(i, p)
          case p: Int => stmt.setInt(i, p)
          case p: Long => stmt.setLong(i, p)
          case p: Short => stmt.setShort(i, p)
          case p: java.sql.SQLXML => stmt.setSQLXML(i, p)
          case p: String => stmt.setString(i, p)
          case p: java.sql.Time => stmt.setTime(i, p)
          case p: java.sql.Timestamp => stmt.setTimestamp(i, p)
          case p: java.net.URL => stmt.setURL(i, p)
          case p: java.util.Date => stmt.setTimestamp(i, new java.sql.Timestamp(p.getTime))
          case p: java.io.InputStream => stmt.setBinaryStream(i, p)
          case p => stmt.setObject(i, p)
        }
      }
    }
  }

  @Nullable
  @inline
  def collectArray[A](rs: ResultSet, @inline extract: ResultSet => A): Array[A] = {
    if (rs.next()) {
      val head = extract(rs)
      val ab = Array.newBuilder[A](ClassTag(head.getClass))
      ab.sizeHint(16)
      ab += head
      while (rs.next())
        ab += extract(rs)
      ab.result()
    } else null.asInstanceOf[Array[A]]
  }

  @inline
  def collectMap[K,V](rs: ResultSet, @inline extract: ResultSet => (K,V)): Map[K,V] = {
    val mb = Map.newBuilder[K,V]
    mb.sizeHint(32)
    while (rs.next())
      mb += extract(rs)
    mb.result()
  }

  @inline
  def collectList[A](rs: ResultSet, @inline extract: ResultSet => A): List[A] = {
    val ab = List.newBuilder[A]
    ab.sizeHint(16)
    while (rs.next())
      ab += extract(rs)
    ab.result()
  }

  @inline
  def collectVec[A](rs: ResultSet, @inline extract: ResultSet => A): Vector[A] = {
    val ab = Vector.newBuilder[A]
    ab.sizeHint(16)
    while (rs.next())
      ab += extract(rs)
    ab.result()
  }

  def collectToMap(implicit rs: ResultSet): Map[String, AnyRef] = {
    import SQLAux._
    implicit val md = rs.getMetaData
    val columnCount = md.getColumnCount
    val mb = Map.newBuilder[String, AnyRef]
    mb.sizeHint(columnCount * 4 / 3 + 1)
    for (i <- 1 to columnCount)
      mb += lookupColumnName(i) -> getResultSetValue(i)
    mb.result()
  }
}
class SQLOperation (val stmt: String, var parameters: Seq[Any] = null) {

  import SQLOperation._

  var queryTimeout = 5

  def bind(params: Seq[Any]): SQLOperation = {
    parameters = params
    this
  }

  //  def batchExe[A](paramsList: Seq[Seq[Any]])(implicit session: DBSession): Array[Int]
  //  = batchExe(paramsList = parameters, process = ps=>ps.executeBatch())(session)

  def exe[A](@inline prepare: Connection=>PreparedStatement,
             @inline process: PreparedStatement => A)
            (implicit session: DBSession): A = {

    val ps = prepare(session.connection)
    SQLOperation.bind(ps, parameters)
    val a = process(ps)
    ps.close()
    a
  }

  def batchExe[A](@inline prepare: Connection=>PreparedStatement,
                  paramsList: GenTraversableOnce[GenTraversableOnce[Any]],
                  @inline process: PreparedStatement => A)(implicit session: DBSession): A = {
    val ps = prepare(session.connection)
    paramsList.foreach(t=>{
      SQLOperation.bind(ps, t)
      ps.addBatch()
    })
    val a = process(ps)
    ps.close()
    a
  }

  var getStmt = (con: Connection)=>{
    val ps = con.prepareStatement(stmt)
    ps.setQueryTimeout(queryTimeout)
    ps
  }

  @inline
  def execute(implicit session: DBSession)
  = exe(getStmt, ps => {
    val hasResult = ps.execute()
    session.checkWarnings(ps)
    if (hasResult)
      ps.getUpdateCount > 0
    else
      true
  })

  var getStmtForInsert = (con: Connection)=>{
    val ps = con.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS)
    ps.setQueryTimeout(queryTimeout)
    ps
  }

  @inline
  def insert[A](@inline extract: ResultSet => A)(implicit session: DBSession): Option[A]
  = exe(getStmtForInsert, ps=>{
    ps.execute()
    session.checkWarnings(ps)
    val rs = ps.getGeneratedKeys
    if (rs.next())
      Some(extract(rs))
    else
      None
  })

  @inline
  def batchInsert(paramsList: GenTraversableOnce[GenTraversableOnce[Any]])(implicit session: DBSession): Array[Int]
  = batchExe(getStmt,
  paramsList,
    ps => {
      val updateCounts = ps.executeBatch()
      session.checkWarnings(ps)
      updateCounts
    })

  @inline
  def batchInsert[A](@inline extract: ResultSet => A,
                     paramsList: GenTraversableOnce[GenTraversableOnce[Any]])(implicit session: DBSession): mutable.WrappedArray[A]
  = batchExe(getStmtForInsert,
    paramsList,
    ps => {
      ps.executeBatch()
      session.checkWarnings(ps)
      collectArray(ps.getGeneratedKeys, extract)
    })

  @inline
  def query[A](mapper: ResultSet => A,
               @inline before: PreparedStatement => Unit)
                (implicit session: DBSession): A
  = exe(getStmt, ps => {
    before(ps)
    val rs = ps.executeQuery()
    session.checkWarnings(ps)
    val ret = mapper(rs)
    rs.close()
    ret
  })

  @inline
  def first[A](@inline extract: ResultSet => A,
               @inline before: PreparedStatement => Unit = NOP[PreparedStatement])
              (implicit session: DBSession): Option[A]
  = query[Option[A]](rs => {
    if (rs.next()) Some(extract(rs)) else None
  }, before)(session)


  @Nullable
  @inline
  def array[A](extract: ResultSet => A,
               before: PreparedStatement => Unit = NOP[PreparedStatement])
              (implicit session: DBSession): Array[A]
  = query[Array[A]](collectArray(_, extract), before)(session)

  @inline
  def list[A](extract: ResultSet => A,
              before: PreparedStatement => Unit = NOP[PreparedStatement])
             (implicit session: DBSession): List[A]
  = query[List[A]](collectList(_, extract), before)(session)

  @inline
  def vector[A](extract: ResultSet => A,
                before: PreparedStatement => Unit = NOP[PreparedStatement])
               (implicit session: DBSession): Vector[A]
  = query[Vector[A]](collectVec(_, extract), before)(session)

  @inline
  def map[K,V](extract: ResultSet => (K,V),
               before: PreparedStatement => Unit = NOP[PreparedStatement])
              (implicit session: DBSession): Map[K,V]
  = query[Map[K,V]](collectMap(_, extract), before)(session)

  def autoMap(before: PreparedStatement => Unit = NOP[PreparedStatement])
         (implicit session: DBSession): Map[String, AnyRef]
  = query[Map[String, AnyRef]](collectToMap(_), before)(session)
}
