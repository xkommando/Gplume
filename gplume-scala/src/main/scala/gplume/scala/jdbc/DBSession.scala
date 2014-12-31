package gplume.scala.jdbc

import java.io.Closeable
import java.sql.{Statement, SQLWarning, SQLException, Connection}
import com.caibowen.gplume.jdbc.JdbcException
import gplume.scala.LogSupport

/**
 * Created by Bowen Cai on 12/27/2014.
 */
class DBSession(val connection: Connection,
                val readOnly: Boolean,
                private[this] var transaction: Option[Transaction] = None)
  extends LogSupport
  with Closeable {

  connection.setReadOnly(readOnly)

  def isTnxActive = transaction.isDefined
  def tnx = transaction

  def checkWarnings(st: Statement) {
    if (log.isDebugEnabled) {
      var warningToLog: SQLWarning = st.getWarnings
      while (warningToLog != null) {
        log.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState + "', error code '" + warningToLog.getErrorCode + "', message [" + warningToLog.getMessage + "]")
        warningToLog = warningToLog.getNextWarning
      }
    }
  }

  def transactional[A](prepare: Connection=>Unit = t=>{
    if (t.getAutoCommit)
      t.setAutoCommit(false)
  }, operation: Transaction => A): A
  = if (isTnxActive) throw new IllegalStateException("Could not begin transaction: session is already transaction active")
    else {

    connection.setAutoCommit(false)
    val tnx = new Transaction(this)
    this.transaction = Some(tnx)
    prepare(connection)
    try {
      val ret = operation(tnx)
      if (log.isTraceEnabled) {
        log.trace(s"Committing JDBC transaction on Connection [$connection]")
      }
      tnx.commit()
      this.transaction = None
      ret
    }
    catch {
      case se: SQLException =>
        if (log.isDebugEnabled) log.debug(s"Initiating transaction rollback [$tnx] on SQLException[$se]")
        tnx.rollback()
        throw se
      case e: Exception =>
        if (log.isDebugEnabled) log.debug(s"Initiating transaction rollback [$tnx] on exception[$e]")
        tnx.rollback()
        throw e
    }
  }


  override def close(): Unit ={
    connection.close()
  }
}
