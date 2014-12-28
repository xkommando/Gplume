package gplume.scala.jdbc

import java.sql.Savepoint

/**
 * Created by Bowen Cai on 12/27/2014.
 */
class Transaction(session: DBSession, isCompleted: Boolean = false, resetAutoCommit: Boolean = true) {

  private[this] var savePointCount = 0

  def save(name: String) = session.connection.setSavepoint(name)
  def save(): Savepoint = save(s"JDBC Session[$session] Savepoint-${savePointCount += savePointCount + 1}")

  def rollback(savepoint: Savepoint) = session.connection.rollback(savepoint)
  def rollback() = session.connection.rollback()

  def releaseSavepoint(savepoint: Savepoint) = session.connection.releaseSavepoint(savepoint)


}
