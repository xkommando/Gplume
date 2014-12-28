package gplume.scala.jdbc

import java.sql.{Statement, SQLWarning, SQLException, Connection}

import gplume.scala.LogSupport

/**
 * Created by Bowen Cai on 12/27/2014.
 */
class DBSession(val connection: Connection) extends LogSupport {

//  lazy val connection: Connection = conn
//
//  private[gplume] val conn: Connection

  def checkWarnings(st: Statement) {
    if (log.isDebugEnabled) {
      var warningToLog: SQLWarning = st.getWarnings
      while (warningToLog != null) {
        log.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState + "', error code '" + warningToLog.getErrorCode + "', message [" + warningToLog.getMessage + "]")
        warningToLog = warningToLog.getNextWarning
      }
    }
  }
}
