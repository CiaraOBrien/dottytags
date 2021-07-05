package dottytags

/*
 * Most of this file is adapted from Scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/Escaping.scala
 */

import scala.quoted.*

private[dottytags] def error(error: String)(using Quotes): Nothing = {
    import quotes.reflect.*
    report.error(error)
    throw scala.quoted.runtime.StopMacroExpansion()
}

private[dottytags] def error(error: String, loc: Expr[Any])(using Quotes): Nothing = {
    import quotes.reflect.*
    report.error(error, loc)
    throw scala.quoted.runtime.StopMacroExpansion()
}

private val tagNameRegex   = "^[a-z][:\\w0-9-]*$".r
private [dottytags] def validateTagName(nameExpr: Expr[String])(using Quotes): String = {
    val name = Phaser.require(nameExpr, "tag name")
    if tagNameRegex.matches(name) then name else error(s"Not a valid XML tag name: $name.", nameExpr)
}

private val attrNameRegex  = "^[a-zA-Z_:][-a-zA-Z0-9_:.]*$".r
private [dottytags] def validateAttrName(nameExpr: Expr[String])(using Quotes): String = {
    val name = Phaser.require(nameExpr, "attribute name")
    if attrNameRegex.matches(name) then name else error(s"Not a valid XML attribute name: $name.", nameExpr)
}

private val styleNameRegex = "^-?[_a-zA-Z]+[_a-zA-Z0-9-]*$".r
private [dottytags] def validateStyleName(nameExpr: Expr[String])(using Quotes): String = {
    val name = Phaser.require(nameExpr, "style name")
    if attrNameRegex.matches(name) then name else error(s"Not a valid CSS style name: $name.", nameExpr)
}

/**
  * Escapes a string for XML, apparently quite quickly, though it should usually not matter
  * since this function should mostly be called at compile-time rather than at runtime.
  * Lifted pretty much directly from scalatags' implementation.
  */
def escapeString(text: String): String = {
    val s = java.io.StringWriter(text.length())
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