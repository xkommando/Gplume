package gplume.scala

import org.slf4j.LoggerFactory

/**
 * Created by Bowen Cai on 12/27/2014.
 */
trait LogSupport {

  protected val log = LoggerFactory.getLogger(this.getClass)

}
