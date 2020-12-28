package dottytags

import scala.quoted._

/**
  * Represents a valid name for an [[dottytags.Core.Attr]] 
  * (that is, a string literal containing a valid XML attribute name).
  * Make your own with [[attr]] or check out [[dottytags.Attrs]] for predefined ones.
  * Unlike [[dottytags.StyleClass$.StyleClass!]] and [[dottytags.TagClass$.TagClass!]],
  * this does not carry any extra configuration payload.
  */ 
final class AttrClass (val name: String) {
  
}

/**
  * Creates a static [[AttrClass!]], suitable for consumption by [[dottytags.Core.:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid XML attribute name.
  */ 
inline def attr(inline name: String): AttrClass = ${ attrValidateMacro('name) }
private def attrValidateMacro(name: Expr[String])(using Quotes): Expr[AttrClass] = {
  import quotes.reflect._
  val sname = name.value.getOrElse(error("Attr name must be a string literal."))
  if !isValidAttrName(sname) then error(s"Not a valid XML attribute name: $name.")
  '{new AttrClass(${Expr(sname)})}
}
given AttrClassFromExpr: FromExpr[AttrClass] with {
  def unapply(x: Expr[AttrClass])(using Quotes): Option[AttrClass] = x match
    case '{ new AttrClass(${Expr(name: String)})} => Some(new AttrClass(name))
}