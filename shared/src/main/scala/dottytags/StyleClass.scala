package dottytags

import scala.quoted._
  
/**
  * Represents a valid name for a [[dottytags.Core.Style]] 
  * (that is, a string literal containing a valid CSS style name).
  * Make your own with [[css]] or [[cssPx]] or check out [[dottytags.Styles]] for predefined ones.
  * Additionally, this can carry a boolean payload indicating that this style takes an argument
  * with a 'px' suffix, which will be automatically added if not present in your argument.
  * [[css]] makes non-px [[StyleClass!]]es, [[cssPx]] makes px ones.
  */
class StyleClass (val name: String, val px: Boolean) {
  
}

/**
  * Creates a static [[StyleClass!]], suitable for consumption by [[dottytags.Core.:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid CSS style name. The resulting [[StyleClass!]] is not px-suffixed,
  * see [[cssPx]] for that.
  */
inline def css(inline name: String): StyleClass = ${ cssValidateMacro('name, '{false}) }
/**
  * Creates a static [[StyleClass!]], suitable for consumption by [[dottytags.Core.:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid CSS style name. The resulting [[TagClass!]] is px-suffixed,
  * see [[css]] for a non-suffixed class.
  */
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