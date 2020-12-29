package dottytags

import scala.quoted._
import scala.collection.mutable.ListBuffer

/**
  * Lifts strings into the the splicing operation, as [[LiftedSplice]] lifts
  * the operation itself.
  * @see [[LiftedStatic]]
  * @see [[LiftedDynamic]]
  */
private[dottytags] trait Span

private[dottytags] object Span {

  /**
    * Lifts an `Expr[String]` into a [[Span]], deferring [[_sticky._splice]]s
    * until runtime. As such, it should primarily be used in cases where it is known
    * that there won't be any nested [[_sticky._splice]]s.
    * @see [[Splice.lift]]
    */
  def lift(s: Expr[String])(using Quotes): Span = s match 
    case Expr(s: String) => LiftedStatic(s)
    case dyn: Expr[String] => LiftedDynamic(dyn)

}
/**
  * Lifts a statically-known string into the splicing context.
  */
private[dottytags] case class LiftedStatic(str: String) extends Span
/**
  * Lifts a dynamic string into the splicing context.
  */
private[dottytags] case class LiftedDynamic(expr: Expr[String]) extends Span
/**
  * Lifts the operation of concatenating strings by concatenating [[Span]]s as much as possible at compile-time, then is
  * decomposed into a [[scala.collections.Seq]] of [[scala.quoted.Expr]]s and inlined at the render site.
  * This whole class is almost definitely an obscene performance bottleneck but it's all at compile-time
  * so lol who cares.
  */
private[dottytags] class LiftedSplice(seq: Seq[Span]) {
  private var parts = ListBuffer().appendAll(seq)
  /**
    * Appends the given [[Span]] to this [[LiftedSplice]] and returns. If `s` is [[LiftedStatic]]
    * then it may be concatenated with the previous [[Span]] if it is also [[LiftedStatic]].
    */
  def append(s: Span): LiftedSplice = { 
    s match
      case LiftedStatic(s)          => parts.lastOption match 
        case Some(LiftedStatic(s1)) => parts.update(parts.size - 1, LiftedStatic(s1 + s))
        case _                      => parts.append(LiftedStatic(s))
      case d: LiftedDynamic         => parts.append(d)
    return this
  }
  /**
    * Prepends the given [[Span]] to this [[LiftedSplice]] and returns. If `s` is [[LiftedStatic]]
    * then it may be concatenated (prepending) with the previous [[Span]] if it is also [[LiftedStatic]].
    */
  def prepend(s: Span): LiftedSplice = { 
    s match 
      case LiftedStatic(s)          => parts.headOption match 
        case Some(LiftedStatic(s1)) => parts.update(0, LiftedStatic(s + s1))
        case _                      => parts.prepend(LiftedStatic(s))
      case d: LiftedDynamic         => parts.prepend(d)
    return this
  }
  /**
    * Adds the given [[LiftedSplice]] to the end of this one by using [[append]] one-by-one.
    */
  def appendAll(s: LiftedSplice): LiftedSplice = {
    s.parts.foreach(append)
    return this
  }
  /**
    * Decomposes this [[LiftedSplice]] into a sequence of expressions yielding strings,
    * [[LiftedStatic]]s are converted into string literals, [[LiftedDynamic]]s into their underlying
    * dynamic expression so it can be inlined.
    */
  def exprs(using Quotes): Seq[Expr[String]] = parts.toList.collect {
    case LiftedStatic(str) => Expr(str)
    case LiftedDynamic(expr) => expr
  }
  /**
    * If the last [[Span]] in this [[LiftedSplice]] is [[LiftedStatic]], strip any trailing
    * spaces for prettiness.
    */
  def stripTrailingSpace: LiftedSplice = {
    parts.lastOption match 
      case Some(LiftedStatic(s)) => parts.update(parts.size - 1, LiftedStatic(s.stripSuffix(" ")))
      case _ =>
    return this
  }
  def isEmpty = parts.isEmpty; def nonEmpty = parts.nonEmpty
}

private[dottytags] object LiftedSplice {
  /**
    * Lifts an `Expr[String]` potentially consisting of one or more [[_sticky._splice]]s,
    * into a [[LiftedSplice]].
    * @see [[Span.lift]]
    */
  def lift(s: Expr[String])(using Quotes): LiftedSplice = s match {
    case Expr(s: String) => LiftedSplice(Seq(LiftedStatic(s)))
    case '{ _sticky._splice(${BetterVarargs(parts)}: _*) } => 
      val ret = LiftedSplice(Nil)
      parts.map(lift).foreach(ret.appendAll)
      ret
    case '{ _sticky._splice($part) } => lift(part)
    case dyn: Expr[String] => LiftedSplice(Seq(LiftedDynamic(dyn)))
  }
}