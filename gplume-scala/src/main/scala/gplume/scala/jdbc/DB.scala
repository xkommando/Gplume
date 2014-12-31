package gplume.scala.jdbc

import java.sql.Connection
import javax.sql.DataSource

/**
 * Created by Bowen Cai on 12/29/2014.
 */
class DB(ds: DataSource) {

  private[this] def withSession[T](session: DBSession, f: DBSession => T): T = {
    var ok = false
    try {
      val res = f(session)
      ok = true
      res
    } finally {
      if(ok) session.close() // Let exceptions propagate normally
      else {
        // f(s) threw an exception, so don't replace it with an Exception from close()
        try session.close() catch { case _: Throwable => }
      }
    }
  }

  def newSession[T](f: DBSession => T): T = withSession(new DBSession(ds.getConnection, false), f)

  def readOnlySession[T](f: DBSession => T): T = withSession(new DBSession(ds.getConnection, true), f)

  def transactional[T](operation: DBSession => T): T
  = withSession(new DBSession(ds.getConnection, false), _.transactional(operation =
    tnx => operation(tnx.session)
  ))

  def execute(sqlStmt: String) = withSession(new DBSession(ds.getConnection, false), new SQLOperation(sqlStmt, null).execute(_))


}
