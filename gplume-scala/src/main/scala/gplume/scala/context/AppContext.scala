package gplume.scala.context

import java.nio.charset.Charset
import java.util.{Date, Calendar, TimeZone}

import com.caibowen.gplume.context.{XMLAssembler, IBeanAssembler}
import com.caibowen.gplume.event.Broadcaster

/**
* @author BowenCai
* @since  12/12/2014.
*/
object AppContext {

  object defaults {
    var timeZone = TimeZone.getTimeZone("GMT")
    private[context] val calendar = Calendar.getInstance(timeZone)
    val charSet = Charset.forName("UTF-8")
  }

  /**
   * thread local variables
   */
  val currentCalendar: ThreadLocal[Calendar] =
    new ThreadLocal[Calendar] {
      protected override def initialValue: Calendar = {
        return Calendar.getInstance(defaults.timeZone)
      }
    }

  /**
   * config file location, written in web.xml
   */
  val MANIFEST = "manifest"
  val LOCALE  = "locale"
  val TIME_ZONE = "timezone"

  val beanAssembler: IBeanAssembler = new XMLAssembler(new gplume.scala.context.SBeanBuilder)

  val broadcaster = new Broadcaster

  def now = defaults.calendar.getTime

}
