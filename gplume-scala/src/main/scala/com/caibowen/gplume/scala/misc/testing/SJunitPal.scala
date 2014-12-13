//package com.caibowen.gplume.scala.misc.testing
//
//import com.caibowen.gplume.misc.testing.junit.{JunitPal => JPal}
//import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider
//
///**
// * @author BowenCai
// * @since  12/12/2014.
// */
//class SJunitPal(c: Class)  extends JPal(c) {
//
//  override def prepareTest (obj: AnyRef, mani: String): Unit = {
//    val booter = new com.caibowen.gplume.scala.context.SContextBooter();
//    booter.setManifestPath(mani);
//    booter.setStreamProvider(new ClassLoaderInputStreamProvider(this.getClass.getClassLoader));
//    booter.boot();
//  }
//}
