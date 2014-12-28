package gplume.scala.test

import org.junit.Test
import gplume.scala.jdbc.SQLInterpolation._
import gplume.scala.jdbc.SQLOperation._
/**
 * Created by Bowen Cai on 12/27/2014.
 */
class SQLInterpTest {

  @Test
  def t1: Unit = {
    val p1 = 5
    val p2 = "fdsrfreg"
    val p3 = List(1)
    val so = sql"SELECT * FROM $p2 WHERE id = $p1 AND $p3"
    println(so.stmt)
  }

  @Test
  def q: Unit ={
//      <prop name="driverClassName" val="com.mysql.jdbc.Driver"/>
//        <prop name="username" val="bitranger" />
//        <prop name="password" val="123456"/>
//        <prop name="jdbcUrl" val="jdbc:mysql://localhost:3306/prma_log_event" />

//    val ds = new HikariDataSource()

  }
}
