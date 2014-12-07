package com.caibowen.gplume.scala.conversion

import java.util.concurrent.Callable

import com.caibowen.gplume.common.Base64

/**
 * @author BowenCai
 * @since  04/12/2014.
 */
object ByteConversions {

  class ByteExt(private[this] val array: Array[Byte] ) {
    def hexlify(from: Int, to: Int): String = {
      val out = new StringBuffer
      for (i <- from until to) {
        val b = array(i)
        val s = (b.toInt & 0xff).toHexString
        if (s.length < 2) {
          out append '0'
        }
        out append s
      }
      out.toString
    }

    def toBase64Str() = Base64.getEncoder.encodeToString(array)

  }


  implicit def ApplyConversion(s: Array[Byte]) = new ByteExt(s)
}
