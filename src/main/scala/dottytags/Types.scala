package dottytags

import scala.quoted.*

/**
 * Anything a [[Tag]] can contain on its own, so attributes, inline CSS styles, child tags, text fragments, etc.
 */
type Entity = Modifier | Content
/**
 * Something a [[Tag]] places within its leading tag brackets rather than between its leading and following tags,
 * so attributes and inline CSS styles.
 */
type Modifier = Attr | Style
/**
 * Something a [[Tag]] places between its leading and following tags to hold it as part of its child contents,
 * so [[Tag]]s, text ([[String]] or [[Raw]]), and [[Frag]]s (which are just an invisible wrapper around some set of [[Content]]s).
 */
type Content = Tag | Frag | Raw | String

/**
 * A wrapper that combines some number of [[Content]] entities into one, which should hopefully be disenspliceable
 * for any [[Tag]] or even other [[Frag]] macro invocation that might enclose it.
 */
final class Frag private[dottytags] (val str: String):
  override def toString: String = str
/** [[Frag]] companion, private to hide [[Frag.apply]] */
private[dottytags] object Frag:
  def apply (s: String): Frag = new Frag(s)

/**
 * A macro that binds a varargs list of [[Content]]s together into one [[Frag]],
 * which is also a [[Content]], and which should produce a disenspliceable splice result.
 * @param parts The [[Content]]s to wrap up in the [[Frag]]. [[String]]s will be ampersand-escaped, or scheduled
 *              to be so at runtime.
 * @return A [[Frag.apply]] invocation whose payload [[String]] expression should be disenspliceable i.e. be an invocation of [[spliceString]].
 */
inline def frag(inline parts: Content*): Frag = ${ fragMacro('parts) }
private def fragMacro(partsExpr: Expr[Seq[Content]])(using Quotes): Expr[Frag] = partsExpr match
  case Varargs(exprs) => '{ Frag(${ Splice.phaseSplice(exprs.map {
    case '{ $str:  String } => PhunctionEscapeStr(Phased(str).maybeEval)
    case '{ Tag($str)     } => Phased(str).maybeEval
    case '{ Raw($str)     } => Phased(str).maybeEval
    case '{ Frag($str)    } => Phased(str).maybeEval
    case '{ $tag:  Tag    } => Phased('{$tag.str}).maybeEval
    case '{ $raw:  Raw    } => Phased('{$raw.str}).maybeEval
    case '{ $frag: Frag   } => Phased('{$frag.str}).maybeEval
    case e => error(s"Unrecognised frag component: ${e.show}", e)
  }*).expr }) }
  case _ => error(s"Input to \"frag\" must be varargs, use \"bind\" instead.", partsExpr)

/**
 * A macro that binds a `Seq[Content]` up into a [[Frag]] with no varargs, and schedules its contents to be spliced together as one
 * span, since it is assumed that it won't happen until runtime anyway.
 */
inline def bind(inline seq: Seq[Content]): Frag = ${ bindMacro('seq) }
private def bindMacro(seqExpr: Expr[Seq[Content]])(using Quotes): Expr[Frag] = '{ Frag(bindContent($seqExpr)) }
private def bindContent(seq: Seq[Content]): String = {
  val sb = new StringBuilder()
  var i = 0
  val len = seq.size
  while i < len do {
      sb.append(seq(i).toString)
      i = i + 1
  }
  sb.toString
}

/** A piece of text that should not be ampersand-escaped. */
final class Raw private[dottytags] (val str: String):
  override def toString: String = str
/** [[Raw]] companion, private to hide [[Raw.apply]] */
private[dottytags] object Raw:
  def apply (s: String): Raw = new Raw(s)

/** Binds its string argument, indicating that it should be incorporated without being ampersand-escaped. */
inline def raw(inline str: String): Raw = ${ rawMacro('str) }
private def rawMacro(expr: Expr[String])(using Quotes): Expr[Raw] = '{ Raw($expr) }
/** Splices its string arguments together using [[Splice]] and indicates that the result need not be ampersand-escaped when incorporated. */
inline def rawSplice(inline parts: String*): Raw = ${ rawSpliceMacro('parts) }
private def rawSpliceMacro(partsExpr: Expr[Seq[String]])(using Quotes): Expr[Raw] = partsExpr match {
  case Varargs(exprs) => '{ Raw(${ Splice.phaseSplice(exprs.map(expr => Phased(expr).maybeEval)*).expr }) }
  case _ => error("\"parts\" must be varargs.", partsExpr)
}