package dottytags

import scala.quoted._

sealed trait Phased[A : Type](using Quotes) {
  def extract: Expr[A] = this match
    case Phased.Static (a:      A ) => Expr(a)
    case Phased.Dynamic(a: Expr[A]) => a
}

object Phased {

  final case class Static [A : Type : ToExpr](val a:      A )(using Quotes) extends Phased[A]
  final case class Dynamic[A : Type](val a: Expr[A])(using Quotes) extends Phased[A]

  def pure[A : Type](x: Expr[A])(using FromExpr[A], Quotes): Phased[A] = x.value match
    case Some(a: A) => Phased.Static(a)(using summon[ToExpr[A]])
    case _          => Phased.Dynamic(x)

  //def flatten[A : Type : FromExpr : ToExpr](p: Phased[Phased[A]])(using Quotes): Phased[A] = p match 
    //case Phased.Static (a:      Phased[A] ) => a
    //case Phased.Dynamic(a: Expr[Phased[A]]) => error("Something's wrong, epic")
  
  inline  def phasedTest(str: String): String = ${ phasedTestImpl('str) }
  private def phasedTestImpl(expr: Expr[String])(using Quotes): Expr[String] = 
    //'{ $expr + ${ Expr(expr.show) } }
    val phased = Phased.pure(expr)
    Expr(phased.toString + "|" + Phased.extract(phased).show)

}