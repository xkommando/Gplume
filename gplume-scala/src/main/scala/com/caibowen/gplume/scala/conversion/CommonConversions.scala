package com.caibowen.gplume.scala.conversion

import java.io.InputStream
import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.concurrent.Callable

import com.caibowen.gplume.common.{Pair => GPair, Triple => GTriple}
import com.caibowen.gplume.context.bean.BeanVisitor
import com.caibowen.gplume.event.{AppEvent, IAppListener, IEventHook}
import com.caibowen.gplume.jdbc.StatementCreator
import com.caibowen.gplume.jdbc.mapper.RowMapping
import com.caibowen.gplume.jdbc.transaction.{Transaction, TransactionCallback}
import com.caibowen.gplume.resource.InputStreamCallback

/**
 * @author BowenCai
 * @since  07/12/2014.
 */
object CommonConversions {

  def toJson(map: Map[Any, Any]): String ={
    val b = new StringBuilder(128)
    b.append("{")
    map.foreach((t:(Any, Any))=>b.append('\"').append(t._1).append("\":\"").append(t._2).append("\",\r\n"))
    b.append("}")
    b.toString
  }
  def toJson(set: Set[Any]): String ={
    val b = new StringBuilder(128)
    b.append("[")
    set.foreach(b.append('\"').append(_).append("\","))
    b.append("]")
    b.toString
  }
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


  // ---------------- JDBC

  @inline
  implicit def makeTnxCallback[T](f: Transaction=> T) = new TransactionCallback[T] {
    override def withTransaction(tnx: Transaction): T = f(tnx)
  }

  @inline
  implicit def makeStatementCreator(f: Connection=>PreparedStatement) = new StatementCreator {
    override def createStatement(con: Connection): PreparedStatement = f(con)
  }

  @inline
  implicit def makeRowMapping[T](f: ResultSet=>T) = new RowMapping[T] {
    override def extract(rs: ResultSet) = f(rs)
  }

  // ---------------- Event

  @inline
  implicit def makeListener(f: AppEvent=>Unit) = new IAppListener[AppEvent] {
    override def onEvent(e: AppEvent) = f(e)
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

  // ---------------- Other

  @inline
  implicit def makeFunction[I,O](f: I => O) = new com.caibowen.gplume.common.Function[I,O] {
    override def apply(input: I): O = f(input)
  }

  @inline
  implicit def makePair[T1,T2](tp: (T1,T2)) = new GPair[T1,T2](tp._1, tp._2)

  @inline
  implicit def makeTriple[T1,T2, T3](tp: (T1,T2, T3)) = new GTriple[T1,T2, T3](tp._1, tp._2, tp._3)

}
