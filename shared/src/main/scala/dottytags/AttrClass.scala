package dottytags

import scala.quoted._

object AttrClass {

  final class AttrClass (val name: String) {
  
  }

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
}