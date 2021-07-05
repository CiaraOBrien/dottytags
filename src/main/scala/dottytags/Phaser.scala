package dottytags

import scala.quoted.*

/**
 * This whole system could be made much more ergonomic probably, but in months of working on it
 * and restarting several times, I have yet to find a particularly ergonomic setup, though
 * this is probably the best it's been.
 */

enum Phaser[A] {
    case ThisPhase[A](val value:     A ) extends Phaser[A]
    case NextPhase[A](val expr: Expr[A]) extends Phaser[A]

    def isDeferred: Boolean = this match
        case Phaser.ThisPhase(_) => false
        case Phaser.NextPhase(_) => true

    def expr(using Quotes, ToExpr[A]): Expr[A] = this match
        case Phaser.ThisPhase(value:     A ) => Expr(value)
        case Phaser.NextPhase(expr: Expr[A]) => expr

    def valueOpt(using Quotes, FromExpr[A]): Option[A] = this match
        case Phaser.ThisPhase(value:     A ) => Some(value)
        case _                               => None

    def defer(using Quotes, ToExpr[A]): Phaser.NextPhase[A] = this match
        case Phaser.ThisPhase(value:     A ) => Phaser.NextPhase(Expr(value))
        case Phaser.NextPhase(expr: Expr[A]) => Phaser.NextPhase(expr)

    def pull(using Quotes, FromExpr[A]): Phaser[A] = this match
        case Phaser.ThisPhase(value:     A ) => Phaser.ThisPhase(value)
        case Phaser.NextPhase(expr: Expr[A]) => expr.value match
            case Some(value: A) => Phaser.ThisPhase(value)
            case _              => Phaser.NextPhase(expr)

    def yank(using Quotes, FromExpr[A]): Phaser.ThisPhase[A] = this match
        case Phaser.ThisPhase(value:     A ) => Phaser.ThisPhase(value)
        case Phaser.NextPhase(expr: Expr[A]) => Phaser.ThisPhase(expr.valueOrError)

}

object Phaser {
    def product[A : Type, B : Type](phaser1: Phaser[A], phaser2: Phaser[B])(using Quotes, ToExpr[A], ToExpr[B]): Phaser[(A, B)] = phaser1 match
        case Phaser.ThisPhase(value1:     A ) => phaser2 match
            case Phaser.ThisPhase(value2:     B ) => Phaser.ThisPhase((value1, value2))
            case Phaser.NextPhase(expr2: Expr[B]) => Phaser.NextPhase('{(${Expr(value1)}, $expr2)})
        case Phaser.NextPhase(expr1: Expr[A]) => phaser2 match
            case Phaser.ThisPhase(value2:     B ) => Phaser.NextPhase('{($expr1, ${Expr(value2)})})
            case Phaser.NextPhase(expr2: Expr[B]) => Phaser.NextPhase('{($expr1, $expr2)})

    def alignAll[A](phasers:     Phaser[A]*)(using Quotes, ToExpr[A]): Seq[Phaser[A]] = alignSeq(phasers)
    def alignSeq[A](phasers: Seq[Phaser[A]])(using Quotes, ToExpr[A]): Seq[Phaser[A]] =
        if phasers.exists(_.isDeferred) then phasers.map(_.defer) else phasers

    def require[A](expr: Expr[A], name: String)(using Quotes, FromExpr[A]): A = expr.value match
        case Some(value: A) => value
        case _ => error(s"Expected a static value for $name.", expr)
}

/**
 * Phunctions carry a static (non-Expr) and a dynamic (Expr) version of their underlying function.
 * Ideally this would be doable automatically, and it is theoretically possible based on my previous attempts,
 * but it's really finnicky and fragile. Probably would need some love on the compiler side of things, or at least
 * the TASTy reflection library.
 */

trait Phunction1[-T, +R] {
    def apply[TT <: T, RR >: R](phased: Phaser[TT])(using Quotes): Phaser[RR] = phased match {
        case Phaser.ThisPhase(value:     TT ) => Phaser.ThisPhase(applyThis(value))
        case Phaser.NextPhase(expr: Expr[TT]) => Phaser.NextPhase(applyNext(expr))
    }
    def applyThis(value:     T )(using Quotes):      R
    def applyNext(expr: Expr[T])(using Quotes): Expr[R]
}

object Phunction1 {
    def apply[T, R](valFn: Function1[T, R], exprFn: Function1[Expr[T], Expr[R]]): Phunction1[T, R] = new Phunction1[T, R] {
        override def applyThis(value:     T )(using Quotes):      R  = valFn(value)
        override def applyNext(expr: Expr[T])(using Quotes): Expr[R] = exprFn(expr)
    }
}

trait Phunction2[-T1, -T2, +R] {
    def apply[TT1 <: T1, TT2 <: T2, RR >: R](phaser1: Phaser[TT1], phaser2: Phaser[TT2])(using Quotes, ToExpr[TT1], ToExpr[TT2]): Phaser[RR] = phaser1 match
        case Phaser.ThisPhase(value1:     TT1 ) => phaser2 match
            case Phaser.ThisPhase(value2:     TT2 ) => Phaser.ThisPhase(applyThis(value1, value2))
            case Phaser.NextPhase(expr2: Expr[TT2]) => Phaser.NextPhase(applyNext(Expr(value1), expr2))
        case Phaser.NextPhase(expr1: Expr[TT1]) => phaser2 match
            case Phaser.ThisPhase(value2:     TT2 ) => Phaser.NextPhase(applyNext(expr1, Expr(value2)))
            case Phaser.NextPhase(expr2: Expr[TT2]) => Phaser.NextPhase(applyNext(expr1, expr2))
    def applyThis(value1:     T1 , value2:     T2 )(using Quotes):      R
    def applyNext(expr1: Expr[T1], expr2: Expr[T2])(using Quotes): Expr[R]
}

object Phunction2 {
    def apply[T1, T2, R](valFn: Function2[T1, T2, R], exprFn: Function2[Expr[T1], Expr[T2], Expr[R]]): Phunction2[T1, T2, R] = new Phunction2[T1, T2, R] {
        override def applyThis(value1:     T1 , value2:     T2 )(using Quotes):      R  = valFn(value1, value2)
        override def applyNext(expr1: Expr[T1], expr2: Expr[T2])(using Quotes): Expr[R] = exprFn(expr1, expr2)
    }
}

object PhunctionStrCat extends Phunction2[String, String, String] {
    override def applyThis(value1:     String , value2:     String )(using Quotes):      String  =   value1 + value2
    override def applyNext(expr1: Expr[String], expr2: Expr[String])(using Quotes): Expr[String] = '{$expr1 + $expr2}
}

object PhunctionEscapeStr extends Phunction1[String, String] {
    override def applyThis(value:     String )(using Quotes):      String  =    escapeString(value)
    override def applyNext(expr: Expr[String])(using Quotes): Expr[String] = '{ escapeString($expr) }
}

object PhunctionToString extends Phunction1[Any, String] {
    override def applyThis(value:     Any )(using Quotes):      String  =    value.toString
    override def applyNext(expr: Expr[Any])(using Quotes): Expr[String] = '{ $expr.toString }
}