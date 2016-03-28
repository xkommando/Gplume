package gplume.scala.context

import java.io.{IOException, File}

import com.caibowen.gplume.context.ContextBooter
import com.caibowen.gplume.resource.{FileInputStreamProvider, InputStreamProviderProxy, ClassLoaderInputStreamProvider}

/**
  * Created by bowen on 3/16/2016.
  */
object ScalaBoot {

  val firstName = "assemble-test.xml"
  val secondName = "assemble.xml"

  def main(args: Array[String]) {
    val booter = new SContextBooter
    val cloader = getClass.getClassLoader
    booter.setClassLoader(cloader)
    val tryNames = (b: SContextBooter, name: String) => {
      val f = new File(name)
      if (f.isFile) {
        b.setManifestPath(name)
        b.setStreamProvider(new InputStreamProviderProxy(new FileInputStreamProvider))
        ContextBooter.LOG.info("Using configuration file[" + f.getAbsolutePath + "]")
        true
      } else if (cloader.getResource(name) != null) {
        b.setManifestPath(name)
        b.setStreamProvider(new InputStreamProviderProxy(new ClassLoaderInputStreamProvider(cloader)))
        ContextBooter.LOG.info("Using configuration file[" + name + "] found in class path")
        true
      } else false
    }
    if (tryNames(booter, firstName) || tryNames(booter, secondName))
      booter.boot()
    else
      throw new IllegalArgumentException(s"cannot find [$firstName] or [$secondName] in classpath or root directory")
  }

}
