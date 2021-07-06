package dottytags

import scala.collection.mutable.ListBuffer
import scala.quoted.*

/**
 * Handles the compile-time splicing-together of adjacent [[Static]] spans, separated by [[Phased]] spans which cannot
 * be spliced until runtime, at which point the whole list of remaining spans, including the [[Static]] ones,
 * should be spliced together in one reasonably efficient operation into one result value.
 */
object Splice {
  /**
   * Splices some arbitrary number of [[Phaser]] spans of a particular [[Spliceable]] type together into one [[Phaser]],
   * which, if it is [[Phased]], can be disenspliced by an enclosing macro invocation to retrieve the original span [[Phaser]]s
   * that were present in this splicing cycle. Thus, an enclosing macro can get full access to every separate static and
   * phased span that resides underneath it, but if the splice is left free to runtime, it will splice properly (see [[spliceString]]).
   * @param spans The spans to be spliced together.
   * @tparam A The [[Spliceable]] type we're interested in splicing. For instance, [[String]].
   * @return A [[Phaser]]; if [[Phased]], it should be able to be disenspliced in future; if [[Static]], then there's
   *         no need since it's just one span anyway.
   */
  def phaseSplice[A](spans: Phaser[A]*)(using q: Quotes, s: Spliceable[A], te: ToExpr[A], fe: FromExpr[A]): Phaser[A] = {
    // Attempt to extract prior splices from spans based on their expressions containing spliceString method calls
    val expanded = spans.flatMap(span => span match {
      case st @ Static(eval: Eval[A]) => Seq(st)
      case      Phased(expr: Expr[A]) => s.disensplice(expr).map(e => Phased(e).maybeEval)
    })
    // This is all pretty unidiomatic sadly, might go back over it at some point
    // Iterate over the list of spans, condensing them into another list, if the next span in `expanded` is static,
    // and the span on top of `condensed` is static, then combine the two onto the top of `condensed` and move on. If
    // the next span in `expanded` is phased, or if there are no spans in `condensed` yet, then we have to put the next span on top of `condensed`.
    var condensed: List[Phaser[A]] = List()
    val iter = expanded.iterator
    while iter.hasNext do {
      iter.next match {
        case phase1 @ Static(eval1: Eval[A]) => condensed.headOption match
          case Some(Static(eval2: Eval[A])) => condensed = Static(s.spliceStatic(eval2, eval1)) :: condensed.tail
          case _ => condensed = phase1 :: condensed
        case phase1 @ Phased(expr1: Expr[A]) => condensed = phase1 :: condensed
      }
    }
    // If there is at least one phased span, then we have to ensplice the whole list,
    // if there are no phased spans then either there is exactly one static span, being the sum of all the other
    // formerly static spans (if there were no phased spans and some static spans), or there are no spans (if there
    // were no spans to begin with).
    if condensed.exists(!_.isStatic) then Phased(s.ensplice(condensed.map(_.expr).reverse))
                                    else condensed.headOption.getOrElse(Static(s.identity))
  }
  
  /**
   * Tries to identify expressions that contain [[spliceString]] invocations (which should therefore have been produced by
   * [[Spliceable.ensplice()]] in a previous macro expansion) and break them back up again into their component static and
   * phased spans, so they can be incorporated into the current splice.
   * @param span A phaser which, if it is phased, might be the remnant of an earlier splice cycle.
   * @return 1 or more spans, 1 if [[span]] was not a splice remnant, more if it was and we were able to disensplice it.
   */
  def phaseDisensplice[A](span: Phaser[A])(using q: Quotes, s: Spliceable[A], fe: FromExpr[A]): Seq[Phaser[A]] = span match {
    case st @ Static(eval: Eval[A]) => Seq(st)
    case      Phased(expr: Expr[A]) => s.disensplice(expr).map(e => Phased(e).maybeEval)
  }
}

/**
 * Basically monoid stuff, plus an agreed-upon protocol for ensplicement and disensplicement (in the case of
 * [[StringSpliceable]] the splicing medium is [[spliceString]], so it is the form into which span lists are enspliced,
 * and it also offers a way to disensplice the exact span expressions that were enspliced by simply examining the varargs.
 * @tparam A The (monoidal) type that we're interested in splicing together
 */
trait Spliceable[A] {
  def identity: A
  def spliceStatic(a1: A, a2: A): A
  def ensplice(spans: Seq[Expr[A]])(using Quotes): Expr[A]
  def disensplice(splice: Expr[A])(using Quotes): Seq[Expr[A]]
}

given StringSpliceable: Spliceable[String] with
  override def identity: String = ""
  override def spliceStatic(a1: String, a2: String): String = a1 + a2
  // Via ensplice and disensplice, spliceString acts not only as the ultimate executor of the splicing process,
  // using a StringBuilder, but it also acts to carry information about the splicing process along the way,
  // since it encodes all the spans it's meant to splice in its varargs, and this can be used to transmit finer-grained
  // splicing information to enclosing macro invocations, whereas normally they'd just be stuck with whatever string
  // expression came out of their sub-invocations (child macros).
  override def ensplice(spans: Seq[Expr[String]])(using Quotes): Expr[String] = '{ spliceString(${Varargs(spans)}: _*) }
  override def disensplice(splice: Expr[String])(using Quotes): Seq[Expr[String]] = splice match {
    case '{spliceString(${Varargs(spans)}: _*)} => spans
    case _ => Seq(splice)
  }

/**
 * Does the dirty work at runtime of actually rolling all the spans into a [[StringBuilder]],
 * but also carries said spans as arguments in its list, so if macro code like [[Spliceable.disensplice]] examines an
 * invocation of [[spliceString]] it can extract all the spans, be they [[Static]] or [[Phased]].
 */
def spliceString(as: String*): String = {
  val sb = new StringBuilder()
  var i = 0
  val len = as.size
  while i < len do {
    sb.append(as(i))
    i = i + 1
  }
  sb.toString
}

