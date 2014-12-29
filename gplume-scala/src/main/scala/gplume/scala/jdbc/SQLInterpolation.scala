package gplume.scala.jdbc


/**
 * Created by Bowen Cai on 12/27/2014.
 */
object SQLInterpolation {

  private case object PaddingParam

  implicit class SQLHelper(val s: StringContext) extends AnyVal {

    def sql(params: Any*): SQLOperation = {
      val sq = params.toSeq
      new SQLOperation(buildQuery(sq), sq)
    }

    private def buildQuery(params: Seq[Any]): String = {
      s.parts.zipAll(params, "", PaddingParam).foldLeft(new StringBuilder(128))(
        (sb, t)=>{
        sb ++= t._1
        addPlaceholders(sb, t._2)
      }).toString
    }

    private def addPlaceholders(sb: StringBuilder, param: Any): StringBuilder = param match {
      // to fix issue #215 due to unexpected Stream#addString behavior
      case s: Stream[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      // Need to convert a Set to a List before mapping to "?", otherwise we end up with a 1-element Set
      case s: Set[_] => addPlaceholders(sb, s.toList) // e.g. in clause
      case t: Traversable[_] => for (i <- 1 until t.size) sb.append('?').append(',')
        sb += '?'
      case `PaddingParam` => sb
      case op: SQLOperation => sb ++= op.stmt
      case _ => sb += '?'
    }
  }

}