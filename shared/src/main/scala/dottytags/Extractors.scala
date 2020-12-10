package dottytags

import scala.quoted._

object ElideNoise {
  def unapply[T](expr: Expr[T])(using Quotes, Type[T]): Option[Expr[T]] = 
    import quotes.reflect._
    def rec(t: Term)(using Quotes): Term = t match {
      case Block(List(), t) => rec(t)
      case Typed(t, _) => rec(t)
      case Inlined(_, List(), t) => rec(t)
      case _ => t
    }
    Some(rec(expr.asTerm).asExprOf[T])
}

object BetterVarargs {

  def apply[T](xs: Seq[Expr[T]])(using Type[T])(using Quotes): Expr[Seq[T]] = {
    import quotes.reflect._
    Repeated(xs.map(Term.of(_)).toList, TypeTree.of[T]).asExpr.asInstanceOf[Expr[Seq[T]]]
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
    rec(Term.of(expr))
  }

}