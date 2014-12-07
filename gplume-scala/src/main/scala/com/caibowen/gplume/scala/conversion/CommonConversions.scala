package com.caibowen.gplume.scala.conversion

import java.io.InputStream
import java.sql.{ResultSet, PreparedStatement, Connection}
import java.util.concurrent.Callable

import com.caibowen.gplume.context.bean.BeanVisitor
import com.caibowen.gplume.event.{AppEvent, IEventHook, IAppListener}
import com.caibowen.gplume.jdbc.StatementCreator
import com.caibowen.gplume.jdbc.mapper.RowMapping
import com.caibowen.gplume.jdbc.transaction.{Transaction, TransactionCallback}
import com.caibowen.gplume.resource.InputStreamCallback

/**
 * @author BowenCai
 * @since  07/12/2014.
 */
object CommonConversions {


  // ---------------- thread
  @inline
  implicit def makeRunnable(f: => Unit): Runnable = new Runnable() { def run() = f }

  @inline
  implicit def makeCallable[T](f: => T): Callable[T] = new Callable[T]() { def call() = f }


  // ---------------- IO
  @inline
  implicit def makeInputStreamCallback(f: InputStream => Unit): InputStreamCallback = new InputStreamCallback {
    override def doInStream(stream: InputStream): Unit = f(stream)
  }

  @inline
  implicit def makeFunction[I,O](f: I => O) = new com.caibowen.gplume.common.Function[I,O] {
    override def apply(input: I): O = f(input)
  }

  // ---------------- JDBC

  @inline
  implicit def makeTnxCallback[T](f: Transaction=> T) = new TransactionCallback[T] {
    override def withTransaction(tnx: Transaction): T = f(tnx)
  }

  @inline
  implicit def StatementCreator(f: Connection=>PreparedStatement) = new StatementCreator {
    override def createStatement(con: Connection): PreparedStatement = f(con)
  }

  @inline
  implicit def makeRowMapping[T](f: ResultSet=>T) = new RowMapping[T] {
    override def extract(rs: ResultSet) = f(rs)
  }

  // ---------------- Event

  @inline
  implicit def makeListener[T](f: T=>Unit) = new IAppListener[T] {
    override def onEvent(e: T) = f(e)
  }

  @inline
  implicit def makeEventHook(f: AppEvent=>Unit) = new IEventHook {
    override def catches(e: AppEvent) = f(e)
  }


  // ---------------- Beans

  @inline
  implicit def makeBeanVisitor[T](f: T=>Unit) = new BeanVisitor[T] {
    override def visit(e: T) = f(e)
  }


}
