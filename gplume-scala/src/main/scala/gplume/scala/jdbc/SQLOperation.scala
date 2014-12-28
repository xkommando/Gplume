package gplume.scala.jdbc

import java.math.MathContext
import java.sql._

/**
 * Created by Bowen Cai on 12/27/2014.
 */
object SQLOperation {

  // single value collectors
  val colBool = (rs: ResultSet) => rs.getBoolean(1)
  val colByte = (rs: ResultSet) => rs.getByte(1)
  val colShort = (rs: ResultSet) => rs.getShort(1)
  val colInt = (rs: ResultSet) => rs.getInt(1)
  val colLong = (rs: ResultSet) => rs.getLong(1)
  val colFloat = (rs: ResultSet) => rs.getFloat(1)
  val colDouble = (rs: ResultSet) => rs.getDouble(1)
  val colStr = (rs: ResultSet) => rs.getString(1)
  val colDate = (rs: ResultSet) => rs.getDate(1)
  val colTime= (rs: ResultSet) => rs.getTime(1)
  val colBytes = (rs: ResultSet) => rs.getBytes(1)
  val colBigDecimal = (rs: ResultSet) => new BigDecimal(rs.getBigDecimal(1), MathContext.DECIMAL128)
  val colObj = (rs: ResultSet) => rs.getObject(1)

  // no oeration
  def NOP[A]: A=>Unit = (a: A)=>{}
//  no return
//  def NRET[A, B]: A=>B = (a:A)=>{null.asInstanceOf[B]}

  def bind(stmt: PreparedStatement, params: Seq[Any]): Unit = {
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
class SQLOperation private[gplume](val stmt: String, var parameters: Seq[Any] = Vector()) {

  import SQLOperation._

  def batchExe[A](prepare: Connection=>PreparedStatement = _.prepareStatement(stmt),
                  paramsList: Seq[Seq[Any]],
                  process: PreparedStatement => A)(implicit session: DBSession): A = {
    val ps = session.connection.prepareStatement(stmt)
    paramsList.foreach(t=>{
      bind(ps, t)
      ps.addBatch()
    })
    val a = process(ps)
    ps.close()
    a
  }

//  def batchExe[A](paramsList: Seq[Seq[Any]])(implicit session: DBSession): Array[Int]
//  = batchExe(paramsList = parameters, process = ps=>ps.executeBatch())(session)

  def exe[A](prepare: Connection=>PreparedStatement = _.prepareStatement(stmt),
             process: PreparedStatement => A)
             (implicit session: DBSession): A = {

    val ps = prepare(session.connection)
    bind(ps, parameters)
    val a = process(ps)
    ps.close()
    a
  }

  def execute(implicit session: DBSession)
  = exe(process = ps => {
    val hasResult = ps.execute()
    session.checkWarnings(ps)
    if (hasResult)
      ps.getUpdateCount > 0
    else
      false
  })

  def insert[A](extract: ResultSet => A)(implicit session: DBSession): Option[A]
  = exe(_.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS), ps=>{
    ps.execute()
    val rs = ps.getGeneratedKeys
    if (rs.next())
      Some(extract(rs))
    else
      None
  })

  def batchInsert[A](extract: ResultSet => A,
                     paramsList: Seq[Seq[Any]])(implicit session: DBSession): List[A]
  = batchExe(_.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS),
    paramsList,
    ps => {
      ps.executeBatch()
      collectList(ps.getGeneratedKeys, extract)
    })

  def query[A](mapper: ResultSet => A,
               before: PreparedStatement => Unit = NOP[PreparedStatement])
              (implicit session: DBSession): A
  = exe(process = ps => {
    before(ps)
    val rs = ps.executeQuery()
    session.checkWarnings(ps)
    val ret = mapper(rs)
    rs.close()
    ret
  })

  def single[A](extract: ResultSet => A,
                before: PreparedStatement => Unit = NOP[PreparedStatement])
               (implicit session: DBSession): Option[A]
  = query[Option[A]](rs => {
    if (rs.next()) Some(extract(rs)) else None
  }, before)(session)


  def collectList[A](rs: ResultSet, extract: ResultSet => A): List[A] = {
    val lsb = List.newBuilder[A]
    lsb.sizeHint(16)
    while (rs.next())
      lsb += extract(rs)
    lsb.result()
  }


  def list[A](extract: ResultSet => A,
              before: PreparedStatement => Unit = NOP[PreparedStatement])
             (implicit session: DBSession): List[A]
  = query[List[A]](collectList(_, extract), before)(session)

}
