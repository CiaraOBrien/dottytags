package dottytags

import scala.quoted._
import scala.compiletime.summonFrom

import cats.Applicative

sealed trait Phased[A : Type : FromExpr : ToExpr](using Quotes) {
  def extract: Expr[A]
}

object Phased {

  final case class Static[A : Type : FromExpr : ToExpr](val a: A)(using Quotes) extends Phased[A] {
    override def extract: Expr[A] = Expr(a)
  }
  final case class Dynamic[A : Type : FromExpr : ToExpr](val a: Expr[A])(using Quotes) extends Phased[A] {
    override def extract: Expr[A] = a
    override def toString: String = {
      val show = a.show
      if show.length < 20 then s"Dynamic(\'{$show})" else "Dynamic(\'{ ... })"
    }
  }

  inline transparent def unlift[A: Type : FromExpr : ToExpr](x: Expr[A])(using Quotes): Phased[A] = x.value match
    case Some(a) => Phased.Static(a)
    case _       => Phased.Dynamic(x)

  def defer[A : Type : FromExpr : ToExpr](x: Expr[A])(using Quotes): Phased[A] = Dynamic(x)

  // This needs a typeclass, PhaseOp, with Option[A => B], Expr[A] => Expr[B], and A => Phased[B],
  // That is: they may be able to apply the operation to a static value, they can defer the operation into a dynamic expression,
  // and they can potentially "contaminate" the output, making it dynamic if it was static before, for operations which either are known to
  // be dymamic or are just not known to be static. A PhaseOp can be generated for any method application,
  // but unknown operations will have no A => B and will therefore dynamicize anything they're applied to.

  // It also needs a way to splice together a list of dynamic and static values.
  // Applicative-ish stuff should be good for that, through something like a traverse or sequence, idk yet.

  //def flatten[A : Type : FromExpr : ToExpr](p: Phased[Phased[A]])(using Quotes): Phased[A] = p match 
    //case Phased.Static (a:      Phased[A] ) => a
    //case Phased.Dynamic(a: Expr[Phased[A]]) => error("Something's wrong, epic")
  
  inline  def phasedTest(str: String): String = ${ phasedTestImpl('str) }
  private def phasedTestImpl(expr: Expr[String])(using Quotes): Expr[String] = 
    //'{ $expr + ${ Expr( expr.show) } }
    val phased = Phased.unlift(expr)
    val phased2 = Phased.defer(expr)
    Expr(phased.toString + "|" + phased2.toString)

}

