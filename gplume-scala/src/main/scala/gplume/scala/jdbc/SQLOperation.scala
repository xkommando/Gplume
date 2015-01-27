package gplume.scala.jdbc

import javax.annotation.Nullable

import gplume.scala.{Tuples, Tuple0}

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

  @Nullable
  @inline
  def collectArray[A](rs: ResultSet, @inline extract: ResultSet => A): Array[A] = {
    if (rs.next()) {
      val head = extract(rs)
      val ab = Array.newBuilder[A](ClassTag(head.getClass))
      ab.sizeHint(32)
      ab += head
      while (rs.next())
        ab += extract(rs)
      ab.result()
    } else null.asInstanceOf[Array[A]]
  }

  @inline
  def collectMap[K,V](rs: ResultSet, @inline extract: ResultSet => (K,V)): Map[K,V] = {
    val mb = Map.newBuilder[K,V]
    mb.sizeHint(64)
    while (rs.next())
      mb += extract(rs)
    mb.result()
  }

  @inline
  def collectList[A](rs: ResultSet, @inline extract: ResultSet => A): List[A] = {
    val ab = List.newBuilder[A]
    ab.sizeHint(32)
    while (rs.next())
      ab += extract(rs)
    ab.result()
  }

  @inline
  def collectVec[A](rs: ResultSet, @inline extract: ResultSet => A): Vector[A] = {
    val ab = Vector.newBuilder[A]
    ab.sizeHint(32)
    while (rs.next())
      ab += extract(rs)
    ab.result()
  }

  def collectToMap(implicit rs: ResultSet): Map[String, Any] = {
    implicit val md = rs.getMetaData
    val columnCount = md.getColumnCount
    if (columnCount <= 0 || !rs.next())
      return Map.empty[String, Any]
    val mb = Map.newBuilder[String, Any]
    mb.sizeHint(columnCount * 4 / 3 + 1)
    for (i <- 1 to columnCount)
      mb += SQLAux.lookupColumnName(i) -> SQLAux.getResultSetValue(i)
    mb.result()
  }

  def collectToProduct(implicit rs: ResultSet): Product = {
    implicit val md = rs.getMetaData
    val columnCount = md.getColumnCount
    if (columnCount <= 0 || !rs.next())
      return Tuple0
    val mb = Array.newBuilder[AnyRef]
    mb.sizeHint(columnCount * 4 / 3 + 1)
    for (i <- 1 to columnCount)
      mb += SQLAux.getResultSetValue(i)
    Tuples.toTuple(mb.result())
  }
}
class SQLOperation (val stmt: String, var parameters: Seq[Any] = Nil) {

  var queryTimeout: Int = 5

  import SQLOperation._

  def bind(params: Any*): SQLOperation = {
    parameters = Seq(params: _*)
    this
  }


  def exe[A](@inline prepare: Connection => PreparedStatement,
             @inline process: PreparedStatement => A)
            (implicit session: DBSession): A = {

    val ps = prepare(session.connection)
    SQLAux.bind(ps, parameters)
    val a = process(ps)
    ps.close()
    a
  }

  def batchExe[A](@inline prepare: Connection => PreparedStatement,
                  paramsList: GenTraversableOnce[GenTraversableOnce[Any]],
                  @inline process: PreparedStatement => A)(implicit session: DBSession): A = {
    val ps = prepare(session.connection)
    paramsList.foreach(t => {
      SQLAux.bind(ps, t)
      ps.addBatch()
    })
    val a = process(ps)
    ps.close()
    a
  }

  var getStmt = (con: Connection) => {
    val ps = con.prepareStatement(stmt)
    ps.setQueryTimeout(queryTimeout)
    ps
  }

  def batchExe[A](paramsList: Seq[Seq[Any]])(implicit session: DBSession): Array[Int]
  = batchExe(getStmt, paramsList, ps => {
    val updateCounts = ps.executeBatch()
    session.checkWarnings(ps)
    updateCounts
  })(session)

  @inline
  def execute(implicit session: DBSession)
  = exe(getStmt, ps => {
    val hasResult = ps.execute()
    session.checkWarnings(ps)
    if (hasResult)
      ps.getUpdateCount > 0
    else
      true
  })(session)

  var getStmtForInsert = (con: Connection) => {
    val ps = con.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS)
    ps.setQueryTimeout(queryTimeout)
    ps
  }

  @inline
  def insert[A](@inline extract: ResultSet => A)(implicit session: DBSession): Option[A]
  = exe(getStmtForInsert, ps => {
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
  def map[K, V](extract: ResultSet => (K, V),
                before: PreparedStatement => Unit = NOP[PreparedStatement])
               (implicit session: DBSession): Map[K, V]
  = query[Map[K, V]](collectMap(_, extract), before)(session)

  def autoMap(before: PreparedStatement => Unit = NOP[PreparedStatement])
             (implicit session: DBSession): Map[String, Any]
  = query[Map[String, Any]](collectToMap(_), before)(session)

  def product(before: PreparedStatement => Unit = NOP[PreparedStatement])
           (implicit session: DBSession): Product
  = query[Product](collectToProduct(_), before)(session)

  //  def int(idx: Int)
  //         (implicit session: DBSession): Int = {
  //    query[Int](rs=>if(rs.next()) rs.getInt(idx) else -1, NOP)(session)
  //  }

}
