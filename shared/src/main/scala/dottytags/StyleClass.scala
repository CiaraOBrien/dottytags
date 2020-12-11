package dottytags

import scala.quoted._
import Core._
  
object StyleClass {

  final class StyleClass (val name: String, val px: Boolean) {
  
  }

  inline def css(inline name: String): StyleClass = ${ cssValidateMacro('name, '{false}) }
  inline def cssPx(inline name: String): StyleClass = ${ cssValidateMacro('name, '{true}) }
  private def cssValidateMacro(name: Expr[String], px: Expr[Boolean])(using Quotes): Expr[StyleClass] = {
    import quotes.reflect._
    val sname = name.value.getOrElse(error("Style name must be a string literal."))
    val spx   = px.value.getOrElse(error("Pixel style must be a boolean literal."))

    if !isValidStyleName(sname) then error(s"Not a valid CSS style name: $name.")
    '{new StyleClass(${Expr(sname)}, ${Expr(spx)})}
  }
  given StyleClassFromExpr: FromExpr[StyleClass] with {
    def unapply(x: Expr[StyleClass])(using Quotes): Option[StyleClass] = x match
      case '{ new StyleClass(${Expr(name: String)}, ${Expr(spx: Boolean)})} => Some(new StyleClass(name, spx))
  }
}