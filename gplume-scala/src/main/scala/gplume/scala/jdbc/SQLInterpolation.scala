package gplume.scala.jdbc


/**
 * Created by Bowen Cai on 12/27/2014.
 */
object SQLInterpolation {

  private case object padding

  implicit class SQLHelper(val s: StringContext) extends AnyVal {

    def sql(params: Any*): SQLOperation = {
      val sq = params.toSeq
      new SQLOperation(buildQuery(sq), sq)
    }

    private def buildQuery(params: Seq[Any]): String = {
      val sb = new StringBuilder(128)
      s.parts.zipAll(params, "", padding).foreach(e => {
        sb ++= e._1
        addPlaceholders(sb, e._2)
      })
      sb.toString
    }

    private def addPlaceholders(sb: StringBuilder, param: Any): StringBuilder = param match {
      // to fix issue #215 due to unexpected Stream#addString behavior
      case s: Stream[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      // Need to convert a Set to a List before mapping to "?", otherwise we end up with a 1-element Set
      case s: Set[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      case t: Traversable[_] => for (i <- 1 until t.size) sb.append('?').append(',')
        sb += '?'
      case padding => sb
      case _ => sb += '?'
    }
  }

}