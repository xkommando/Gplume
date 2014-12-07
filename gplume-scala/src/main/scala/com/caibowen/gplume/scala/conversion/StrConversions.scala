package com.caibowen.gplume.scala.conversion

import com.caibowen.gplume.common.Base64
import com.caibowen.gplume.misc.Str._

/**
 * @author BowenCai
 * @since  04/12/2014.
 */
object StrConversions {

  class StrExt(private[this] val s: String) {

    def levenshteinDistance(s2: String, threshold: Int) = Math.getLevenshteinDistance(s, s2, threshold)
    def jaroWinklerDistance(s2: String) = Math.getJaroWinklerDistance(s, s2)

    def notBlank = Utils.notBlank(s)
    def isBlank = !Utils.notBlank(s)
    def isEmail = Patterns.EMAIL.matcher(s).matches()
    def isNumber = Utils.isDigits(s)
    def isAllUpperCase = Utils.isAllUpperCase(s)
    def isAllLowerCase = Utils.isAllLowerCase(s)
    def isDigits = Utils.isDigits(s)
    def substrBefore(separator: String) = Utils.substrBefore(s, separator)
    def substrAfter(separator: String) = Utils.substrAfter(s, separator)

    def toIpV4 = Utils.ipV4ToLong(s)

    /**
     * Turn a string of hex digits into a byte array. This does the exact
     * opposite of `Array[Byte]#hexlify`.
     */
    def unhexlify(): Array[Byte] = {
      val buffer = new Array[Byte]((s.length + 1) / 2)
      (s.grouped(2).toSeq zipWithIndex) foreach { case (substr, i) =>
        buffer(i) = Integer.parseInt(substr, 16).toByte
      }
      buffer
    }

    def decodeBase64 = Base64.getDecoder.decode(s)

  }

  implicit def ApplyConversion(s: String) = new StrExt(s)

}
