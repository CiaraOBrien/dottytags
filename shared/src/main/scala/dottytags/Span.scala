package dottytags

import scala.quoted._
import scala.collection.mutable.ListBuffer

/**
  * Something that can be [[Splice]]d. Intended for use at compile-time.
  * @see [[Static]]
  * @see [[Dynamic]]
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
    case Expr(s: String) => Static(s)
    case dyn: Expr[String] => Dynamic(dyn)

}
/**
  * Something that can be [[Splice]]d with adjacent `Static`s at compile-time since it is statically-known.
  * Intended for use at compile-time.
  */
private[dottytags] case class Static(str: String) extends Span
/**
  * Something that can only be [[Splice]]d at runtime.
  *  Intended for use at compile-time.
  */
private[dottytags] case class Dynamic(expr: Expr[String]) extends Span
/**
  * Splices together [[Span]]s as much as possible at compile-time, then is
  * decomposed into a [[scala.collections.Seq]] of [[scala.quoted.Expr]]s and inlined at the render site.
  * This whole class is almost definitely an obscene performance bottleneck but it's all at compile-time
  * so lol who cares.
  */
private[dottytags] class Splice(seq: Seq[Span]) {
  private var parts = ListBuffer().appendAll(seq)
  /**
    * Appends the given [[Span]] to this [[Splice]] and returns. If `s` is [[Static]]
    * then it may be concatenated with the previous [[Span]] if it is also [[Static]].
    */
  def append(s: Span): Splice = { 
    s match
      case Static(s) => parts.lastOption match 
        case Some(Static(s1)) => parts.update(parts.size - 1, Static(s1 + s))
        case _                => parts.append(Static(s))
      case d: Dynamic         => parts.append(d)
    return this
  }
  /**
    * Prepends the given [[Span]] to this [[Splice]] and returns. If `s` is [[Static]]
    * then it may be concatenated (prepending) with the previous [[Span]] if it is also [[Static]].
    */
  def prepend(s: Span): Splice = { 
    s match 
      case Static(s) => parts.headOption match 
        case Some(Static(s1)) => parts.update(0, Static(s + s1))
        case _                => parts.prepend(Static(s))
      case d: Dynamic         => parts.prepend(d)
    return this
  }
  /**
    * Adds the given [[Splice]] to the end of this one by using [[append]] one-by-one.
    */
  def appendAll(s: Splice): Splice = {
    s.parts.foreach(append)
    return this
  }
  /**
    * Decomposes this [[Splice]] into a sequence of expressions yielding strings,
    * [[Static]]s are converted into string literals, [[Dynamic]]s into their underlying
    * dynamic expression so it can be inlined.
    */
  def exprs(using Quotes): Seq[Expr[String]] = parts.toList.collect {
    case Static(str) => Expr(str)
    case Dynamic(expr) => expr
  }
  /**
    * If the last [[Span]] in this [[Splice]] is [[Static]], strip any trailing
    * spaces for prettiness.
    */
  def stripTrailingSpace: Splice = {
    parts.lastOption match 
      case Some(Static(s)) => parts.update(parts.size - 1, Static(s.stripSuffix(" ")))
      case _ =>
    return this
  }
  def isEmpty = parts.isEmpty; def nonEmpty = parts.nonEmpty
}

private[dottytags] object Splice {
  /**
    * Lifts an `Expr[String]` potentially consisting of one or more [[_sticky._splice]]s,
    * into a [[Splice]].
    * @see [[Span.lift]]
    */
  def lift(s: Expr[String])(using Quotes): Splice = s match {
    case Expr(s: String) => Splice(Seq(Static(s)))
    case '{ _sticky._splice(${BetterVarargs(parts)}: _*) } => 
      val ret = Splice(Nil)
      parts.map(lift).foreach(ret.appendAll)
      ret
    case '{ _sticky._splice($part) } => lift(part)
    case dyn: Expr[String] => Splice(Seq(Dynamic(dyn)))
  }
}