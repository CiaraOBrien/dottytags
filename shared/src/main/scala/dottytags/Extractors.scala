package dottytags

import scala.quoted._

/**
  * [[scala.quoted.Varargs]] but better (that is, more versatile). This improvement has been merged
  * into dotty, so when 3.0.0-RC1 is released, this will go away.
  */
object BetterVarargs {

  def apply[T](xs: Seq[Expr[T]])(using Type[T])(using Quotes): Expr[Seq[T]] = {
    import quotes.reflect._
    Repeated(xs.map(_.asTerm).toList, TypeTree.of[T]).asExpr.asInstanceOf[Expr[Seq[T]]]
  }

  def unapply[T](expr: Expr[Seq[T]])(using Quotes): Option[Seq[Expr[T]]] = {
    import quotes.reflect._
    def rec(tree: Term): Option[Seq[Expr[T]]] = tree match {
      case Repeated(elems, _) => Some(elems.map(x => x.asExpr.asInstanceOf[Expr[T]]))
      case Typed(e, _) => rec(e)
      case Block(List(), e) => rec(e)
      case Inlined(_, List(), e) => rec(e)
      case _  => None
    }
    rec(expr.asTerm)
  }

}