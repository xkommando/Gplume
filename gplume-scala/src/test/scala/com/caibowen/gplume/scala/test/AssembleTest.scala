package com.caibowen.gplume.scala.test

import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider
import com.caibowen.gplume.scala.context.{AppContext, ContextBooter}

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
//  val js = Serialization.writePretty(obj)(DefaultFormats)
  println(obj.map)
  println(obj.lsmap)
//  val js = JSON.toJSONString(obj, true)
//  println(js)
//  <dependency>
//    <groupId>net.liftweb</groupId>
//    <artifactId>lift-json_2.10</artifactId>
//    <version>2.5.1</version>
//    <scope>test</scope>
//  </dependency>
//    <dependency>
//      <groupId>com.alibaba</groupId>
//      <artifactId>fastjson</artifactId>
//      <version>1.1.15</version>
//    </dependency>
  println("Done")
}
