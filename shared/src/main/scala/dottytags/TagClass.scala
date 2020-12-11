package dottytags

import scala.quoted._
import Core._

object TagClass {

  class TagClass (val name: String, val selfClosing: Boolean) {

  }

  inline def tag(inline name: String): TagClass = ${ tagValidateMacro('name) }
  private def tagValidateMacro(name: Expr[String])(using Quotes): Expr[TagClass] = {
    import quotes.reflect._
    val sname = name.value.getOrElse(error("Tag name must be a string literal."))
    if !isValidTagName(sname) then error(s"Not a valid XML tag name: $name.")
    '{new TagClass(${Expr(sname)}, ${Expr(false)})}
  }
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

}