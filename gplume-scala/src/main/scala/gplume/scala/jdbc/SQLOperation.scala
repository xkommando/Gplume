package gplume.scala.jdbc

import javax.annotation.Nullable

import scala.collection.{mutable, GenTraversableOnce}
import java.math.MathContext
import java.sql.{Connection, Statement, PreparedStatement, ResultSet, Time, Timestamp}

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
          case p: java.util.Date => stmt.setTimestamp(i, new Timestamp(p.getTime))
          case p: java.io.InputStream => stmt.setBinaryStream(i, p)
          case p => stmt.setObject(i, p)
        }
      }
    }
  }
}
class SQLOperation (val stmt: String, var parameters: Seq[Any] = Vector()) {

  import SQLOperation.NOP

  private[this] var ps: PreparedStatement = _

  def bind(params: GenTraversableOnce[Any],
           @inline prepare: Connection=>PreparedStatement = _.prepareStatement(stmt))
          (implicit session: DBSession): SQLOperation = {

    ps = prepare(session.connection)
    SQLOperation.bind(ps, params)
    this
  }

  def batchExe[A](@inline prepare: Connection=>PreparedStatement = _.prepareStatement(stmt),
                  paramsList: GenTraversableOnce[GenTraversableOnce[Any]],
                  @inline process: PreparedStatement => A)(implicit session: DBSession): A = {
    ps = prepare(session.connection)
    paramsList.foreach(t=>{
      SQLOperation.bind(ps, t)
      ps.addBatch()
    })
    val a = process(ps)
    ps.close()
    a
  }

//  def batchExe[A](paramsList: Seq[Seq[Any]])(implicit session: DBSession): Array[Int]
//  = batchExe(paramsList = parameters, process = ps=>ps.executeBatch())(session)

  def exe[A](@inline prepare: Connection=>PreparedStatement = _.prepareStatement(stmt),
            @inline process: PreparedStatement => A)
             (implicit session: DBSession): A = {

    ps = prepare(session.connection)
    SQLOperation.bind(ps, parameters)
    val a = process(ps)
    ps.close()
    a
  }

  @inline
  def execute(implicit session: DBSession)
  = exe(process = ps => {
    val hasResult = ps.execute()
    session.checkWarnings(ps)
    if (hasResult)
      ps.getUpdateCount > 0
    else
      true
  })

  @inline
  def insert[A](@inline extract: ResultSet => A)(implicit session: DBSession): Option[A]
  = exe(_.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS), ps=>{
    ps.execute()
    val rs = ps.getGeneratedKeys
    if (rs.next())
      Some(extract(rs))
    else
      None
  })

  @inline
  def batchInsert(paramsList: GenTraversableOnce[GenTraversableOnce[Any]])(implicit session: DBSession): Array[Int]
  = batchExe(paramsList = paramsList,
    process = ps => {
      ps.executeBatch()
    })

  @inline
  def batchInsert[A](@inline extract: ResultSet => A,
                     paramsList: GenTraversableOnce[GenTraversableOnce[Any]])(implicit session: DBSession): mutable.WrappedArray[A]
  = batchExe(_.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS),
    paramsList,
    ps => {
      ps.executeBatch()
      collectArray(ps.getGeneratedKeys, extract)
    })

  @inline
  def query[A](mapper: ResultSet => A,
              @inline before: PreparedStatement => Unit = NOP[PreparedStatement])
              (implicit session: DBSession): A
  = exe(process = ps => {
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

  @inline
  def first[A](@inline extract: ResultSet => A, default: A,
                @inline before: PreparedStatement => Unit = NOP[PreparedStatement])
               (implicit session: DBSession): A
  = query[A](rs => {
    if (rs.next()) extract(rs) else default
  }, before)(session)

  @Nullable
  @inline
  def collectArray[A](rs: ResultSet, @inline extract: ResultSet => A): mutable.WrappedArray[A] = {
    if (rs.next()) {
      val head = extract(rs)
      val ab = Array.newBuilder[A](ClassTag(head.getClass))
      ab.sizeHint(16)
      ab += head
      while (rs.next())
        ab += extract(rs)
      mutable.WrappedArray.make(ab.result())
    } else null.asInstanceOf[Array[A]]
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

  @Nullable
  @inline
  def array[A](extract: ResultSet => A,
              before: PreparedStatement => Unit = NOP[PreparedStatement])
             (implicit session: DBSession): mutable.WrappedArray[A]
  = query[mutable.WrappedArray[A]](collectArray(_, extract), before)(session)

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
}
