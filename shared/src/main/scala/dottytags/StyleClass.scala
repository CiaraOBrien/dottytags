package dottytags

import scala.quoted._


  
object StyleClass {

  final class StyleClass (val name: String) {
  
  }

  inline def css(inline name: String): StyleClass = ${ cssValidateMacro('name) }
  private def cssValidateMacro(name: Expr[String])(using Quotes): Expr[StyleClass] = {
    import quotes.reflect._
    val sname = name.value.getOrElse(error("Style name must be a string literal."))
    if !isValidStyleName(sname) then error(s"Not a valid CSS style name: $name.")
    '{new StyleClass(${Expr(sname)})}
  }
  given StyleClassFromExpr: FromExpr[StyleClass] with {
    def unapply(x: Expr[StyleClass])(using Quotes): Option[StyleClass] = x match
      case '{ new StyleClass(${Expr(name: String)})} => Some(new StyleClass(name))
  }
}