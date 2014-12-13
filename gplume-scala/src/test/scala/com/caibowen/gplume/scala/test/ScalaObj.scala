package com.caibowen.gplume.scala.test

import com.caibowen.gplume.context.bean.{IDAwareBean, InitializingBean}

/**
* @author BowenCai
* @since  12/12/2014.
*/
class ScalaObj(list: List[String], doubleVal: Double) extends InitializingBean with IDAwareBean {

  var lsmap: List[AnyRef] = _
  var map: Map[String, AnyRef] = _
  var id: String = _

  def start: Unit = {
    println(s"${classOf[ScalaObj]} start")
  }

  override def afterPropertiesSet: Unit ={
    println(s"${classOf[ScalaObj]} afterPropertiesSet")
  }

  def setBeanID(id: String): Unit = {
    this.id = id
  }
}
