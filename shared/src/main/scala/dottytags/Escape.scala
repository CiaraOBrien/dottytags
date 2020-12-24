package dottytags

import java.io.StringWriter

/*
 * Most of this file is adapted from scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/Escaping.scala
 */

private val tagNameRegex   = "^[a-z][:\\w0-9-]*$".r
private val attrNameRegex  = "^[a-zA-Z_:][-a-zA-Z0-9_:.]*$".r
private val styleNameRegex = "^-?[_a-zA-Z]+[_a-zA-Z0-9-]*$".r
private[dottytags] def isValidTagName(name: String): Boolean = tagNameRegex.matches(name)
private[dottytags] def isValidAttrName(name: String): Boolean = attrNameRegex.matches(name)
private[dottytags] def isValidStyleName(name: String): Boolean = styleNameRegex.matches(name)
private[dottytags] def camelCase(dashedString: String) = {
  val list = dashedString.split("-").nn.toList.map(_.nn)
  (list.head :: list.tail.map(s => s(0).toUpper.toString + s.drop(1))).mkString
}
/** 
  * Escapes a string for XML, apparently quite quickly, though it should usually not matter
  * since this function should mostly be called at compile-time rather than at runtime.
  * Lifted pretty much directly from scalatags' implementation. 
  */
def escape(text: String): String = {
    val s = StringWriter(text.length())
    // Implemented per XML spec:
    // http://www.w3.org/International/questions/qa-controls
    // Highly imperative code, ~2-3x faster than the previous implementation (2020-06-11)
    val charsArray = text.toCharArray.nn
    val len = charsArray.size.nn
    var pos = 0
    var i = 0
    while i < len do {
      val c = charsArray(i)
      c match {
        case '<' =>
          s.write(charsArray, pos, i - pos)
          s.write("&lt;")
          pos = i + 1
        case '>' =>
          s.write(charsArray, pos, i - pos)
          s.write("&gt;")
          pos = i + 1
        case '&' =>
          s.write(charsArray, pos, i - pos)
          s.write("&amp;")
          pos = i + 1
        case '"' =>
          s.write(charsArray, pos, i - pos)
          s.write("&quot;")
          pos = i + 1
        case '\n' =>
        case '\r' =>
        case '\t' =>
        case c if c < ' ' =>
          s.write(charsArray, pos, i - pos)
          pos = i + 1
        case _ =>
      }
      i = i + 1
    }
    // Apparently this isn't technically necessary if (len - pos) == 0 as
    // it doesn't cause any exception to occur in the JVM.
    // The problem is that it isn't documented anywhere so I left this if here
    // to make the error clear.
    if pos < len then s.write(charsArray, pos, len - pos)
    s.toString
  }
