package gplume.scala

import java.lang.reflect.Constructor

/**
 * Created by Bowen Cai on 1/24/2015.
 */
sealed trait Product0 extends Any with Product {
  def productArity = 0
  def productElement(n: Int) = throw new IllegalStateException("No element")
  def canEqual(that: Any) = false
}

object Tuple0 extends Product0 {
  override def toString() = "()"
}

case class SeqProduct(elems: Any*) extends Product {
  override def productArity: Int = elems.size
  override def productElement(i: Int) = elems(i)
  override def toString() = elems.addString(new StringBuilder(elems.size * 8 + 10), "(" , ",", ")").toString()
}

object Tuples {

  private[this] val ctors = {
    val ab = Array.newBuilder[Constructor[_]]
    for (i <- 1 to 22) {
      val tupleClass = Class.forName("scala.Tuple" + i)
      ab += tupleClass.getConstructors.apply(0)
    }
    ab.result()
  }

  def toTuple(elems: Seq[AnyRef]): Product = elems.length match {
    case 0 => Tuple0
    case size if size <= 22 =>
      ctors(size - 1).newInstance(elems: _*).asInstanceOf[Product]
    case size if size > 22 => new SeqProduct(elems: _*)
  }

}