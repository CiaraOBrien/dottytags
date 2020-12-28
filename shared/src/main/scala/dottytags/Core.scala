package dottytags

import scala.quoted._
import scala.annotation.targetName
import scala.unchecked
import scala.language.implicitConversions
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/** 
  * "Sticky Note" methods that are inserted into the macro expansion as short-lived tags
  * which do not make it into the final output and which should not be used by the calling
  * code (hence the mangled names). They must be public though, since they are called from
  * macro-generated code inlined at the call site. This is extremely cursed and maybe eventually I'll
  * get around to making it nicer, more elegant, and maybe improve the API at the same time!
  */
private[dottytags] object _sticky {
  /**
    * At compile-time, acts as a flag that its arguments must be spliced, and is removed if
    * the splice is seamless (no dynamic chunks leftover, everything can be concatenateed into one big string literal).
    * At runtime, finalizes the splicing process, concatenating the 
    * compile-time-concatenated static chunks with their dynamic neighbors.
    */
  def _splice(parts: String*): String = {
    val sb = new StringBuilder()
    var i = 0
    val len = parts.size
    while i < len do {
      sb.append(parts(i))
      i = i + 1
    }
    sb.toString
  }
  /**
    * Experiment to see if using single methods is better than inlining some of this stuff.
    * @see [[Core.unstick_dyn_frag]]
    */
  def _splice_escaping(parts: Seq[Element]): String = {
    val sb = new StringBuilder()
    var i = 0
    val len = parts.size
    while i < len do {
      sb.append(parts(i) match
        case s: String => dottytags.escape(s)
        case e         => _sticky._renderOutOfLine(e)
      )
      i = i + 1
    }
    sb.toString
  }
  /**
    * At compile-time, acts as a flag that its argument expression represents an attribute,
    * and is usually removed by the enclosing [[Core.tag]] invocation.
    * At runtime, initializes a naked dynamic [[Attr]] to hold its computed contents.
    */
  def _attr       (s: String): Attr   = new Attr(s)
  /**
    * At compile-time, acts as a flag that its argument expression represents a style,
    * and is usually removed by the enclosing [[Core.tag]] invocation.
    * At runtime, initializes a naked dynamic [[Style]] to hold its computed contents.
    */
  def _style      (s: String): Style  = new Style(s)
  /**
    * At compile-time, acts as a flag that its argument expression represents a string which should not be escaped,
    * and is usually removed by the enclosing [[Core.tag]] invocation.
    * At runtime, initializes a naked dynamic [[Raw]] to hold its computed contents.
    */
  def _raw        (s: String): Raw    = new Raw(s)
  /**
    * At compile-time, acts as a flag that its argument expression represents a tag,
    * and is usually removed by the enclosing [[Core.tag]] invocation.
    * At runtime, initializes a naked dynamic [[Tag]] to hold its computed contents.
    * This is the only one of the sticky methods besides [[_splice]] and [[_frag]] which is expected to usually show up
    * in the final macro-expanded code. The overhead of allocating a few [[Tag]]s here and there is
    * probably negligible. It would be possible to get the [[Core.render]] method to elide
    * the top-level sticky method if appropriate, but that would be a waste of everyone's time.
    */
  def _tag        (s: String): Tag    = new Tag(s)
  /**
    * At compile-time, acts as a flag that its sequence of argument expression represents a frag,
    * and is sometimes removed by the enclosing [[Core.tag]] invocation.
    * At runtime, initializes a naked dynamic [[Frag]] to hold its computed contents.
    * This is the only one of the sticky methods besides [[_splice]] and [[_tag]] which is expected to usually show up
    * in the final macro-expanded code. The overhead of allocating a few [[Frag]]s here and there is
    * probably negligible.
    */
  def _frag (s: Seq[Element]): Frag   = new Frag(s)
  /**
    * Actually renders an [[Entity]] to a [[String]] at runtime, without getting inlined. 
    * This isn't intended for client code to use, [[Core.render]] already falls through to this
    * inline for entities whose exact time isn't statically known.
    * @see [[Core.render]]
    */
  def _renderOutOfLine(e: Entity): String = e match
    case a: Attr   => a.str
    case s: Style  => s.str
    case r: Raw    => r.str
    case t: Tag    => t.str
    case f: Frag   => _sticky._splice(f.s.map(_renderOutOfLine): _*)
    case s: String => s
  /**
    * @see [[dottytags.Core.pxifyDynamic]]
    */
  def _pxifyOutOfLine(s: String): String = if s.endsWith("px") then s else s + "px"
  /**
    * The true nature of an attribute value. If you can see this as the type of an object at runtime, you're probably
    * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
    */
  final class Attr  private[_sticky] (val str: String)     { override def toString = str }
  /**
    * The true nature of a style value. If you can see this as the type of an object at runtime, you're probably
    * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
    */
  final class Style private[_sticky] (val str: String)     { override def toString = str }
  /**
    * The true nature of a raw value. If you can see this as the type of an object at runtime, you're probably
    * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
    */
  final class Raw   private[_sticky] (val str: String)     { override def toString = str }
  /**
    * The true nature of the result of splicing a tag tree. Unlike [[Attr]], [[Style]], [[Raw]], and [[Frag]],
    * it's normal to see this quite often as the type of an object at runtime, though of course you should call
    * [[Core.render]] rather than screwing around with a naked tag.
    */
  final class Tag   private[_sticky] (val str: String)     { override def toString = str }
  /**
    * The true nature of a frag value. If you can see this as the type of an object at runtime, you're probably
    * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
    */
  final class Frag  private[_sticky] (val s: Seq[Element]) { override def toString = s.mkString }
  // Prevent the generation of constructor proxy objects, which break the export clause
  object Attr; object Style; object Raw; object Tag; object Frag;
  /** 
    * An [[Entity]] that is not allowed to be part of a [[Frag]].
    * We use types instead of trait inheritance (at the moment) because we need to 
    * transparently include [[String]]s in the type hierarchy.
    * @see [[Element]]
    */
  type Modifier = Attr | Style
  /** 
    * An [[Entity]] that is allowed to be part of a [[Frag]].
    * We use types instead of trait inheritance (at the moment) because we need to 
    * transparently include [[String]]s in the type hierarchy.
    * @see [[Modifier]]
    */
  type Element = Raw | Tag | Frag | String
  /** 
    * Something [[String]]y (see [[dottytags.Core.render]]) that represents, in and of itself, a coherent
    * component of an XML tree. [[dottytags.TagClass!]]es, [[dottytags.AttrClass!]]es, and [[dottytags.StyleClass!]]es do not
    * make the cut because while they are valid names for XML objects, they cannot meaningfully exist
    * on their own among their peers. We use types instead of trait inheritance (at the moment) because we need to 
    * transparently include [[String]]s in the type hierarchy.
    * @see [[Modifier]]
    * @see [[Element]]
    */
  type Entity = Modifier | Element
}

export _sticky.{Attr, Style, Raw, Tag, Frag, Modifier, Element, Entity}

/**
  * "Renders" an [[Entity]] to a [[String]]. Of course, most entities are to a large extent
  * pre-rendered at compile-time if you're using the library right, so this is mostly just
  * a unified way of turning an already-[[String]]y datatype into a proper [[String]]. This
  * will hopefully get inlined to either a call to the relevant getter method or a call to
  * [[_sticky._splice]], but falls through to a call to [[_sticky._renderOutOfLine]] when the type of
  * `e` isn't statically known at compile-time.
  * @see [[_sticky._renderOutOfLine]]
  */
extension (inline e: Entity) inline def render: String = inline e match
  case a: Attr   => a.str
  case s: Style  => s.str
  case r: Raw    => r.str
  case t: Tag    => t.str
  case f: Frag   => _sticky._splice(f.s.map(_sticky._renderOutOfLine): _*)
  case s: String => s
  case _         => _sticky._renderOutOfLine(e)

/**
  * Private convenience method for erroring out during macro expansion.
  * @param error the error message to display
  * @throws [[scala.quoted.runtime.StopMacroExpansion]] always
  */
private[dottytags] def error(error: String)(using Quotes): Nothing = {
  import quotes.reflect._
  report.error(error)
  throw scala.quoted.runtime.StopMacroExpansion()
}

/** 
  * Shortcut for [[_sticky._splice]] invocations in macros.
  * If `parts` only contains one `Expr[String]`, passes it through without splicing.
  */
private def stick_splice(parts: Expr[String]*) (using Quotes): Expr[String] =  parts match 
                    case    Seq(head: Expr[String])                         => head
                    case s: Seq[Expr[String]]                               => '{ _sticky._splice(${Varargs(s)}: _*) }
/** Shortcut for [[_sticky._attr]] invocations in macros. */
private def stick_attr  (s: Expr[String])      (using Quotes): Expr[Attr]   =  '{ _sticky._attr ($s) }
/** Shortcut for [[_sticky._style]] invocations in macros. */
private def stick_style (s: Expr[String])      (using Quotes): Expr[Style]  =  '{ _sticky._style($s) }
/** Shortcut for [[_sticky._raw]] invocations in macros. */
private def stick_raw   (s: Expr[String])      (using Quotes): Expr[Raw]    =  '{ _sticky._raw  ($s) }
/** Shortcut for [[_sticky._tag]] invocations in macros. */
private def stick_tag   (s: Expr[String])      (using Quotes): Expr[Tag]    =  '{ _sticky._tag  ($s) }
/** Shortcut for [[_sticky._frag]] invocations in macros. */
private def stick_frag  (s: Expr[Seq[Element]])(using Quotes): Expr[Frag]   =  '{ _sticky._frag ($s) }

/** 
  * This is pretty much where all the magic happens, everything that can be statically determined gets
  * concatenated at compile-time, then everything gets spliced together with [[_sticky._splice]], which boils down to building a [[StringBuilder]]
  * from an [[Array]], where the array elements alternate between pre-concatenated static chunks and bits of code that generate
  * the dynamic elements.
  * 
  * @example This: 
  * ```scala
  * println(html(cls := "foo", href := "bar", css("baz1") := "qux", "quux", 
  *   System.currentTimeMillis.toString, css("baz2") := "qux", raw("a")
  * ).render)
  * ```
  * Boils down to something like:
  * ```scala
  * println(dottytags.Core._sticky._tag(
  *   dottytags.Core._sticky._splice(
  *     scala.runtime.ScalaRunTime.wrapRefArray(
  *       ["<html class=\"foo\" href=\"bar\" style=\"baz: qux;\">quux",
  *       dottytags.escape(scala.Long.box(System.currentTimeMillis()).toString())
  *         , "a</html>"]
  *     )
  *   )
  * ).str())
  * ```
  * Which, when run, yields:
  * ```html
  * <html class="foo" href="bar" style="baz1: qux; baz2: qux;">quux1608810396295a</html>
  * ```
  */
inline implicit def tagClass2EmptyTag(inline cl: TagClass): Tag = cl()
extension (inline cl: TagClass) inline def apply(inline args: Entity*): Tag = ${ tagMacro('cl)('args) }
private def tagMacro(cl: Expr[TagClass])(args: Expr[Seq[Entity]])(using Quotes): Expr[Tag] = {
  import quotes.reflect._
  val cls = cl.value.getOrElse(error("Tag class must be static."))
  val sname       = cls.name
  val selfClosing = cls.selfClosing

  val attrs  = Splice(List(Static(s"<$sname ")))
  val styles = Splice(Nil)
  val body   = Splice(Nil)
  
  def iter(as: Seq[Expr[Entity]]): Unit = as.foreach{_ match 
    // Static attr/style/tag/raw string:
    case '{_sticky._attr($s)}  => attrs .appendAll(Splice.lift(s))
    case '{_sticky._style($s)} => styles.appendAll(Splice.lift(s))
    case '{_sticky._tag($s)}   => body  .appendAll(Splice.lift(s))
    case '{_sticky._raw($s)}   => body  .appendAll(Splice.lift(s))
    // Static frag, can be decomposed:
    case '{_sticky._frag(${BetterVarargs(s)})} => iter(s)
    // Dynamic attr/style/tag/raw string:
    case '{$e: Attr}  => attrs .append(Span.lift('{$e.str}))
    case '{$e: Style} => styles.append(Span.lift('{$e.str}))
    case '{$e: Tag}   => body  .append(Span.lift('{$e.str}))
    case '{$e: Raw}   => body  .append(Span.lift('{$e.str}))
    // Static frag with a dynamic interior, _frag call can be elided by extracting the Expr[Seq[Element]]
    case '{_sticky._frag(${e: Expr[Seq[Element]]})} => body.append(Dynamic('{_sticky._splice_escaping($e)}))
    // Dynamic frag, must be completely spliced (and escaped) at runtime:
    case '{$e: Frag} => body.append(Dynamic('{_sticky._splice_escaping($e.s)}))
    // Static or dynamic non-raw string (must be escaped):
    case '{$e: String} => body.append(Span.lift(e) match 
      case Static(str) => Static(escape(str))
      case Dynamic(e) => Dynamic('{escape($e)}))
    // Lol idk
    case e => error("Error, unable to expand expression:\n" + e.show)
  }

  BetterVarargs.unapply(args).map { varargs => iter(varargs)
    if styles.isEmpty then attrs.stripTrailingSpace
                      else styles.stripTrailingSpace.prepend(Static("style=\"")).append(Static("\""))
    if body.isEmpty   then body.prepend(Static(if selfClosing then "/>" else s"></$sname>"))
                      else body.prepend(Static(">")).append(Static(s"</$sname>"))
    stick_tag(stick_splice(attrs.appendAll(styles).appendAll(body).exprs: _*))
  }.getOrElse(error("Error, unable to expand varargs:\n" + args.show))
}

/** The simplest macro, just tags `raw` with [[stick_raw]] */
inline def raw(inline raw: String): Raw = ${ rawMacro('raw) }
private def rawMacro(raw: Expr[String])(using Quotes): Expr[Raw] = stick_raw(raw)

/** 
  * Binds up the given elements, escaping any non-[[raw]] strings pre-emptively.
  * This cannot handle being passed actual [[Seq]]s due to the vicissitudes of varargs, for that you'll want [[bind]].
  * @see[[bind]]
  */
inline def frag(inline elems: Element*): Frag = ${ fragMacro('elems) }
/** This could probably be fixed so you don't need to bind() and can just frag() seqs directly, but for now,
  * since using `: _*` doesn't work with [[frag]]'s varargs for some reason, if your args are not var, use this.
  * @see [[frag]]
  */
inline def bind(inline elems: Seq[Element]): Frag = ${ fragMacro('elems) }
private def fragMacro(elems: Expr[Seq[Element]])(using Quotes): Expr[Frag] = elems match 
  case BetterVarargs(as) => stick_frag(BetterVarargs(as))
  case e: Expr[Seq[Element]] => stick_frag(e)
  case e => error("Error, unable to expand:\n" + e.show)
private def fragMacroVals(elems: Expr[Seq[AnyVal]])(using Quotes): Expr[Frag] = elems match 
  case BetterVarargs(as) => stick_frag(BetterVarargs(as.map(a => '{$a.toString})))
  case e: Expr[Seq[AnyVal]] => stick_frag('{$e.map(_.toString)})
  case e => error("Error, unable to expand:\n" + e.show)

/** Just checks `attr` is static, escapes `value`, and then splices together the attribute string.
  * If we move to a less cursed system using actual case classes or whatever then this can become a member method of [[AttrClass!]].
  */
extension (inline attr: AttrClass) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ attrMacro('attr, 'value) }
private def attrMacro(attr: Expr[AttrClass], setTo: Expr[String])(using Quotes): Expr[Attr] = attr.value.map { cls => setTo.value match 
  case Some(setTo: String) => stick_attr(Expr(cls.name + "=\"" + escape(setTo) + "\" "))
  case _ => stick_attr(stick_splice(Expr(cls.name + "=\""), '{escape($setTo)}, Expr("\" ")))
}.getOrElse(error("Attribute class must be static."))

/**
  * Just checks `style` is static, escapes `value` (dynamic or static), 
  * adds the px suffix (dynamically or statically) if `style` says so 
  * and then splices together the style string.
  * If we move to a less cursed system using actual case classes or whatever then this can become a member method of [[StyleClass!]].
  */
extension (inline style: StyleClass) @targetName("setStyle") inline def :=(inline value: String): Style = ${ styleMacro('style, 'value) }
private def styleMacro(style: Expr[StyleClass], setTo: Expr[String])(using Quotes): Expr[Style] = style.value.map { cls => setTo.value match
  case Some(setTo: String) => stick_style(Expr(cls.name + ": " + pxifyStatic(escape(setTo), cls.px) + "; "))
  case _ => stick_style(stick_splice(Expr(cls.name + ": "), pxifyDynamic('{escape($setTo)}, cls.px), Expr("; ")))
}.getOrElse(error("Style class must be static."))
/** Pxifies a static string: if it doesn't already end with 'px', add it */
private def pxifyStatic (s: String, px: Boolean): String  = if px && !s.endsWith("px") then s + "px" else s
/**
  * Pxifies a dynamic string: if, at runtime, its computed value doesn't already end with 'px', add it.
  * @see [[_sticky._pxifyOutOfLine]]
  */
private def pxifyDynamic(s: Expr[String], px: Boolean)(using Quotes): Expr[String] = if px then '{ _sticky._pxifyOutOfLine($s) } else s


inline def concatStrLits(inline str1: String, inline str2: String): String = ${ concatStrLitsMacro('str1, 'str2) }
private def concatStrLitsMacro(str1: Expr[String], str2: Expr[String])(using Quotes): Expr[String] = 
  Expr(str1.value.getOrElse(error("str1 must be static.")) + str2.value.getOrElse(error("str2 must be static.")))

inline def showCode(inline a: Any): Unit = ${ showCodeMacro('a) }
private def showCodeMacro(a: Expr[Any])(using Quotes): Expr[Unit] = {
  import quotes.reflect._
  '{
    println(s"Code for ${${Expr(Position.ofMacroExpansion.startLine.toString)}}:")
    println(${Expr(Printer.TreeAnsiCode.show(a.asTerm))})
    ()
  }
}

inline def showTree(inline a: Any): Unit = ${ showTreeMacro('a) }
private def showTreeMacro(a: Expr[Any])(using Quotes): Expr[Unit] = {
  import quotes.reflect._
  '{
    println(s"Tree for ${${Expr(Position.ofMacroExpansion.startLine.toString)}}:")
    println(${Expr(Printer.TreeStructure.show(a.asTerm))})
    ()
  }
}