package gplume.scala.context

import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider

/**
  * Created by bowen on 3/16/2016.
  */
object ScalaBoot {

  def main(args: Array[String]) {
    val booter = new SContextBooter
    val cloader = getClass.getClassLoader
    booter.setClassLoader(cloader)
    booter.setStreamProvider(new ClassLoaderInputStreamProvider(cloader))
    booter.setManifestPath("assemble.xml")
    booter.boot()
  }
}
