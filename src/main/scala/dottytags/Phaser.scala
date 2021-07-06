package dottytags

import scala.quoted.*

/**
 * This whole system could be made much more ergonomic probably, but in months of working on it
 * and restarting several times, I have yet to find a particularly ergonomic setup, though
 * this is probably the best it's been. This whole "Phaser" concept is pretty much just Applicative,
 * but the Compiler God demands boilerplate, it's very difficult to get a "static" function `A => B` and the
 * corresponding "deferred" function `Expr[A] => Expr[B]` (essentially a function over expressions that sets up the
 * former function to be executed at runtime on the value of the expression in question) without having to type
 * both out and carry them around in a datatype, hence [[Phunction1]] and [[Phunction2]].
 */

/**
 * Represents an [[Expr]] we encounter during macro expansion, and the possibility to discern its static value, or
 * to fail to discern that value (because it can only be known at runtime, for any number of reasons), and the possibility
 * to do the same calculations on it regardless, either performing the operations on the static value during compile-time,
 * or "scheduling" the same operations to be carried out in order at runtime.
 */
sealed abstract class Phaser[+A] protected () {
  def isStatic: Boolean
  /** Option for if we have a value available (if this [[Phaser]] is [[Static]]) */
  def evalOpt: Option[Eval[A]]
}

/** An expression whose value we can determine at compile-time using [[FromExpr]]s, so we can do computations on it directly. */
final case class Static[+A](val eval: Eval[A]) extends Phaser[A] {
  override def isStatic: Boolean = true
  override def evalOpt: Option[Eval[A]] = Some(eval)
}
/** Literally just so I don't have to worry about aligning `A` with `Expr[A]` in parallel lines of code, instead it just stays aligned automatically. */
type Eval[A] = A

/** An expression whose value we cannot determine right now, perhaps it's a variable reference or maybe it's just
 * shrouded by an unknown method call, either way we only know its type, but we can do things with it anyway by
 * wrapping its expression in method calls and stuff, so we can "schedule" computations to be done on it, once its value is known
 * at runtime. This enables us to do the same computations on both scrutible and inscrutible expressions, so we
 * can abstract over compile-time legibility, which is the whole point of this paradigm. */
final case class Phased[+A](val expr: Expr[A]) extends Phaser[A] {
  override def isStatic: Boolean = false
  override def evalOpt: Option[Eval[A]] = None
}

/** [[Phaser]] companion, can be entirely public, doesn't need to hide anything while retaining visibility via macro method call injection */
object Phaser {
  /** It's very natural to do Applicative type stuff with [[Phaser]], but you have to avoid the urge in this case because
   * generating a bunch of code to fuck around with tuples at runtime would be counterproductive. */
  def product[A : Type, B : Type](phaser1: Phaser[A], phaser2: Phaser[B])(using Quotes, ToExpr[A], ToExpr[B]): Phaser[(A, B)] = phaser1 match
    case Static(eval1: Eval[A]) => phaser2 match
      case Static(eval2: Eval[B]) => Static((eval1, eval2))
      case Phased(expr2: Expr[B]) => Phased('{(${Expr(eval1)}, $expr2)})
    case Phased(expr1: Expr[A]) => phaser2 match
      case Static(eval2: Eval[B]) => Phased('{($expr1, ${Expr(eval2)})})
      case Phased(expr2: Expr[B]) => Phased('{($expr1, $expr2)})
  
  /** If any of the (varargs) [[Phaser]]s are [[Phased]], defer them all */
  def alignAll[A](phasers:     Phaser[A]*)(using Quotes, ToExpr[A]): Seq[Phaser[A]] = alignSeq(phasers)
  /** If any of the (seq of) [[Phaser]]s are [[Phased]], defer them all */
  def alignSeq[A](phasers: Seq[Phaser[A]])(using Quotes, ToExpr[A]): Seq[Phaser[A]] =
    if phasers.exists(!_.isStatic) then phasers.map(_.defer) else phasers
  
  /** If we cannot evaluate the given [[Expr]], error saying we expected a [[Static]] value for the given name. */
  def require[A](expr: Expr[A], name: String)(using Quotes, FromExpr[A]): Eval[A] = expr.value.getOrElse(error(s"Expected a static value for $name.", expr))
}

/** These have to be extensions because of variance issues */
extension [A](p: Phaser[A])(using Quotes) {
  
  /** Convert a [[Static]] into a [[Phased]] using a [[ToExpr]], or just leave a [[Phased]] be. */
  def defer(using ToExpr[A]): Phased[A] = Phased(p.expr)
  
  /** Derive an [[Expr]] for this [[Phaser]], possibly using a [[ToExpr]], this is generally how you turn
   *  your [[Phaser]]s back into mere expressions when it's time to let them back out into the wild outside this macro expansion context. */
  def expr(using ToExpr[A]): Expr[A] = p match
    case Static(eval: Eval[A]) => Expr(eval)
    case Phased(expr: Expr[A]) => expr
  
  /** Attempt to make this [[Phaser]] become a [[Static]] by reading it with a [[FromExpr]], but if not that's fine. */
  def maybeEval(using FromExpr[A]): Phaser[A] = p match
    case s @ Static(eval: Eval[A]) => s
    case p @ Phased(expr: Expr[A]) => expr.value.fold(p)(a => Static(a))
  
  /** Attempt to make this [[Phaser]] become a [[Static]] by reading it with a [[FromExpr]], and error if we can't. */
  def mustEval(name: String)(using FromExpr[A]): Static[A] = p match
    case s @ Static(eval: Eval[A]) => s
    case p @ Phased(expr: Expr[A]) => Static(expr.value.getOrElse(error(s"Expected a static value for $name.", expr)))
  
  def show: String = p match
    case Static(eval: Eval[A]) => s"Static(${eval.toString})"
    case Phased(expr: Expr[A]) => s"Phased(${expr.show})"
  
}

/**
 * Phunctions carry a static (non-Expr) and a dynamic (Expr) version of their underlying function.
 * Ideally this would be doable automatically, and it is theoretically possible based on my previous attempts,
 * but it's really finnicky and fragile. Probably would need some love on the compiler side of things, or at least
 * the TASTy reflection library.
 */
trait Phunction1[-T, +R] {
  def apply(phased: Phaser[T])(using Quotes): Phaser[R] = phased match
    case Static(eval: Eval[T]) => Static(applyThis(eval))
    case Phased(expr: Expr[T]) => Phased(applyNext(expr))
  def applyThis(eval: Eval[T])(using Quotes): Eval[R]
  def applyNext(expr: Expr[T])(using Quotes): Expr[R]
}

/** [[Phunction1]] companion, no need to be hidden */
object Phunction1 {
  def apply[T, R](evalFn: Function1[T, R], exprFn: Function1[Expr[T], Expr[R]]): Phunction1[T, R] = new Phunction1[T, R] {
    override def applyThis(eval: Eval[T])(using Quotes): Eval[R] = evalFn(eval)
    override def applyNext(expr: Expr[T])(using Quotes): Expr[R] = exprFn(expr)
  }
}

/** Like [[Phunction1]], but more! */
trait Phunction2[-T1, -T2, +R] {
  def apply[TT1 <: T1, TT2 <: T2, RR >: R](phaser1: Phaser[TT1], phaser2: Phaser[TT2])(using Quotes, ToExpr[TT1], ToExpr[TT2]): Phaser[RR] = phaser1 match
    case Static(eval1: Eval[TT1]) => phaser2 match
      case Static(eval2: Eval[TT2]) => Static(applyThis(eval1, eval2))
      case Phased(expr2: Expr[TT2]) => Phased(applyNext(Expr(eval1), expr2))
    case Phased(expr1: Expr[TT1]) => phaser2 match
      case Static(eval2: Eval[TT2]) => Phased(applyNext(expr1, Expr(eval2)))
      case Phased(expr2: Expr[TT2]) => Phased(applyNext(expr1, expr2))
  def applyThis(eval1: Eval[T1], eval2: Eval[T2])(using Quotes): Eval[R]
  def applyNext(expr1: Expr[T1], expr2: Expr[T2])(using Quotes): Expr[R]
}

/** [[Phunction2]] companion, no need to be hidden */
object Phunction2 {
  def apply[T1, T2, R](evalFn: Function2[T1, T2, R], exprFn: Function2[Expr[T1], Expr[T2], Expr[R]]): Phunction2[T1, T2, R] = new Phunction2[T1, T2, R] {
    override def applyThis(eval1: Eval[T1], eval2: Eval[T2])(using Quotes): Eval[R] = evalFn(eval1, eval2)
    override def applyNext(expr1: Expr[T1], expr2: Expr[T2])(using Quotes): Expr[R] = exprFn(expr1, expr2)
  }
}

// Various occasionally-useful Phunctions, they're too boilerplatey to just declare whenever you need them,
// you pretty much have to stick them in their own corner or they get pretty yucky-looking

/** Phunction to concatenate two strings */
object PhunctionStrCat extends Phunction2[String, String, String] {
  override def applyThis(eval1: Eval[String], eval2: Eval[String])(using Quotes): Eval[String] =    eval1 +  eval2
  override def applyNext(expr1: Expr[String], expr2: Expr[String])(using Quotes): Expr[String] = '{$expr1 + $expr2}
}

/** Phunction to ampersand-escape a string */
object PhunctionEscapeStr extends Phunction1[String, String] {
  override def applyThis(eval: Eval[String])(using Quotes): Eval[String] =    escapeString(eval)
  override def applyNext(expr: Expr[String])(using Quotes): Expr[String] = '{ escapeString($expr) }
}

/** Phunction to call `toString` on anything */
object PhunctionToString extends Phunction1[Any, String] {
  override def applyThis(eval: Eval[Any])(using Quotes): Eval[String] =     eval.toString
  override def applyNext(expr: Expr[Any])(using Quotes): Expr[String] = '{ $expr.toString }
}