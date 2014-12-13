package com.caibowen.gplume.scala.context

import com.caibowen.gplume.context.{ContextBooter => JBooter, ConfigCenter}
import com.caibowen.gplume.misc.Str
import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider

/**
 * @author BowenCai
 * @since  12/12/2014.
 */
class ContextBooter extends JBooter {

  override def boot() : Unit = {
    // set classloader for beanAssembler
    val configCenter = new ConfigCenter();
    configCenter.setClassPathProvider(new ClassLoaderInputStreamProvider(this.classLoader));
    configCenter.setDefaultStreamProvider(streamProvider);

    AppContext.beanAssembler.setClassLoader(this.classLoader);
    AppContext.beanAssembler.setConfigCenter(configCenter);

    if (Str.Utils.notBlank(manifestPath)) {
      try {
        AppContext.beanAssembler.assemble(manifestPath);
      } catch {
        case e: Throwable =>
        throw new RuntimeException("Error building beans", e);
      }

    } else {
      JBooter.LOG.warn("no manifest file specified "
        + "For web application, check your web.xml for context-param[{0}]"
        + AppContext.MANIFEST);
      return;
    }

    // register listeners
    AppContext.beanAssembler
      .inTake(AppContext.broadcaster.listenerRetreiver);
  }

}
