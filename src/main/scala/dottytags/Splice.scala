package dottytags

import scala.collection.mutable.ListBuffer
import scala.quoted.*

object Splice {
  def phaseSplice[A](spans: Phaser[A]*)(using q: Quotes, s: Spliceable[A], te: ToExpr[A], fe: FromExpr[A]): Phaser[A] = {
    // Attempt to extract prior splices from spans based on their expressions containing spliceString method calls
    val expanded = spans.flatMap(span => span match {
      case tp @ Phaser.ThisPhase(_: A) => Seq(tp)
      case      Phaser.NextPhase(expr: Expr[A]) => s.disensplice(expr).map(e => Phaser.NextPhase(e).pull)
    })
    // This is all pretty unidiomatic sadly, might go back over it at some point
    var condensed: List[Phaser[A]] = List()
    val iter = expanded.iterator
    while iter.hasNext do {
      iter.next match {
        case phase1 @ Phaser.ThisPhase(value1: A) => condensed.headOption match
          case Some(Phaser.ThisPhase(value2: A)) => condensed = Phaser.ThisPhase(s.spliceStatic(value2, value1)) :: condensed.tail
          case _ => condensed = phase1 :: condensed
        case phase1 @ Phaser.NextPhase(expr1: Expr[A]) => condensed = phase1 :: condensed
      }
    }
    if condensed.exists(_.isDeferred) then Phaser.NextPhase(s.ensplice(condensed.map(_.expr).reverse))
                                      else condensed.headOption.getOrElse(Phaser.ThisPhase(s.identity))
  }
  def phaseDisensplice[A](splice: Phaser[A])(using q: Quotes, s: Spliceable[A], fe: FromExpr[A]): Seq[Phaser[A]] = splice match {
    case tp @ Phaser.ThisPhase(_: A) => Seq(tp)
    case Phaser.NextPhase(expr: Expr[A]) => s.disensplice(expr).map(e => Phaser.NextPhase(e).pull)
  }
}

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

