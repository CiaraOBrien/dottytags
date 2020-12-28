package dottytags

import scala.quoted._

/**
  * Represents a valid name for a [[dottytags.Core.Tag]] 
  * (that is, a string literal containing a valid XML tag name).
  * Make your own with [[tag]] or check out [[dottytags.Tags]] for predefined ones.
  * Additionally, this can carry a boolean payload indicating that this tag is an HTML
  * self-closing tag, like `<br/>`. You can make your own self-closing tags with the overload of [[tag]]
  * that takes an extra boolean argument.
  */
class TagClass (val name: String, val selfClosing: Boolean) {

}

/**
  * Creates a static [[TagClass!]], suitable for consumption by [[dottytags.Core.apply]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid XML tag name. The resulting [[TagClass!]] is not self-closing,
  * see the other overload for that.
  */
inline def tag(inline name: String): TagClass = ${ tagValidateMacro('name) }
private def tagValidateMacro(name: Expr[String])(using Quotes): Expr[TagClass] = {
  import quotes.reflect._
  val sname = name.value.getOrElse(error("Tag name must be a string literal."))
  if !isValidTagName(sname) then error(s"Not a valid XML tag name: $name.")
  '{new TagClass(${Expr(sname)}, ${Expr(false)})}
}
/**
  * Creates a static [[TagClass!]], suitable for consumption by [[dottytags.Core.apply]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * if `name` is not a valid XML tag name, or if `sc` is not a boolean literal or other static
  * boolean value. The resulting [[TagClass!]] is self-closing if `sc == true`.
  */
inline def tag(inline name: String, inline sc: Boolean): TagClass = ${ tagValidateMacroSC('name, 'sc) }
private def tagValidateMacroSC(name: Expr[String], sc: Expr[Boolean])(using Quotes): Expr[TagClass] = {
  import quotes.reflect._
  val sname = name.value.getOrElse(error("Tag name must be a string literal."))
  if !isValidTagName(sname) then error(s"Not a valid XML tag name: $name.")
  val scs  = sc.value.getOrElse(error("Self-closing flag must be a literal."))
  '{new TagClass(${Expr(sname)}, ${Expr(scs)})}
}
given TagClassFromExpr: FromExpr[TagClass] with {
  def unapply(x: Expr[TagClass])(using Quotes): Option[TagClass] = x match
    case '{ new TagClass(${Expr(name: String)}, ${Expr(b)}: Boolean) } => Some(new TagClass(name, b))
}