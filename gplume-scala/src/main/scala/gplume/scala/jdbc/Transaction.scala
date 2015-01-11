package gplume.scala.jdbc

import java.sql.Savepoint

/**
 * Created by Bowen Cai on 12/27/2014.
 */
class Transaction private[gplume](val session: DBSession,
                                  // <config>
                                  var resetAutoCommit: Boolean,
                                  var rollBackOnly: Boolean,
                                 // </config>
                                  private[this] val previousISOLevel: Int,
                                  private[this] var isCompleted: Boolean) {

  def this(session: DBSession) {
    this(session, true, false, session.connection.getTransactionIsolation, false)
  }

  def commit(): Unit ={
    if (isCompleted)
      throw new IllegalStateException(s"Could not commit transaction in session[$session]: already completed")
    if (rollBackOnly)
      return rollback()

    session.connection.commit()
    complete()
  }

  private[this] var savePointCount = 0

  def save(name: String) = session.connection.setSavepoint(name)
  def save(): Savepoint = {
    savePointCount += savePointCount + 1
    save(s"JDBC Session[$session] Savepoint-${savePointCount}")
  }

  def rollback(savepoint: Savepoint): Unit = {
    if (isCompleted)
      throw new IllegalStateException(s"Could not rollback transaction in session[$session] to savepoint[$savepoint]: already completed")
    session.connection.rollback(savepoint)
    complete()
  }

  def rollback(): Unit = {
    if (isCompleted)
      throw new IllegalStateException(s"Could not rollback transaction in session[$session]: already completed")
    session.connection.rollback()
    complete()
  }

  def releaseSavepoint(savepoint: Savepoint) = session.connection.releaseSavepoint(savepoint)

  private def complete(): Unit ={
    val con = session.connection
    if (previousISOLevel != con.getTransactionIsolation)
      con.setTransactionIsolation(previousISOLevel)
    if (resetAutoCommit)
      con.setAutoCommit(true)
    if (session.readOnly && !con.isReadOnly)
      con.setReadOnly(session.readOnly)
    isCompleted = true
  }

}
