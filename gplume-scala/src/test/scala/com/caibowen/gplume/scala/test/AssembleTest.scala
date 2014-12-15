package com.caibowen.gplume.scala.test

import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider
import com.caibowen.gplume.scala.context.{AppContext, ContextBooter}
import net.liftweb.json.{DefaultFormats, Serialization}

/**
 * @author BowenCai
 * @since  12/12/2014.
 */
object AssembleTest extends App {

  val booter = new ContextBooter()
  booter.setManifestPath("classpath:test_scala_assemble.xml")
  val _ss = this.getClass.getClassLoader
  booter.setStreamProvider(new ClassLoaderInputStreamProvider(_ss))
  booter.setClassLoader(_ss)
  booter.boot()

  Console.setOut(System.err)

  val obj = AppContext.beanAssembler.getBean("testObj").asInstanceOf[ScalaObj]
  val js1 = Serialization.writePretty(obj)(DefaultFormats)
//  println(obj.map)
//  println(obj.lsmap)
//  val js2 = JSON.toJSONString(obj, true)
  println(js1)
//  println("\r\n\r\n\r\n")
//  println(js2)

  println("Done")
}







