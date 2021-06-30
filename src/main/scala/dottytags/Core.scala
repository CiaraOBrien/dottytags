package dottytags

import scala.quoted.*
import scala.annotation.targetName
import scala.unchecked
import scala.language.implicitConversions
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

import utils.*

// === Basic Datatypes === //

/**
  * The true nature of an attribute value. If you can see this as the type of an object at runtime, you're probably
  * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked. 
  */
final class Attr private[dottytags] (val str: String) { 
  override def toString = str
}
private[dottytags] object Attr {
  /**
    * At compile-time, Attr(expr) acts as a flag that its argument expression represents an attribute,
    * and is usually removed by the enclosing [[tag]] invocation.
    * At runtime, initializes a naked dynamic [[Attr]] to hold its computed contents.
    */
  def apply (s: String): Attr = new Attr(s)
}

/**
  * The true nature of a style value. If you can see this as the type of an object at runtime, you're probably
  * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
  */
final class Style private[dottytags] (val str: String) { 
  override def toString = str 
}
private[dottytags] object Style {
  /**
    * At compile-time, `Style(expr)` acts as a flag that its argument expression represents a style,
    * and is usually removed by the enclosing [[tag]] invocation.
    * At runtime, initializes a naked dynamic [[Style]] to hold its computed contents.
    */
  def apply (s: String): Style = new Style(s)
}
/**
  * The true nature of a raw value. If you can see this as the type of an object at runtime, you're probably
  * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
  */
final class Raw private[dottytags] (val str: String) { 
  override def toString = str 
}
private[dottytags] object Raw {
  /**
    * At compile-time, Raw(expr) acts as a flag that its argument expression represents a string which should not be escaped,
    * and is usually removed by the enclosing [[tag]] invocation.
    * At runtime, initializes a naked dynamic [[Raw]] to hold its computed contents.
    */
  def apply (s: String): Raw = new Raw(s)
}

/**
  * The true nature of the result of splicing a tag tree. Unlike [[Attr]], [[Style]], [[Raw]], and [[Frag]],
  * it's normal to see this quite often as the type of an object at runtime, though of course you should call
  * [[render]] rather than screwing around with a naked tag.
  */
final class Tag private[dottytags] (val str: String) { 
  override def toString = str 
}
private[dottytags] object Tag {
  /**
    * At compile-time, `Tag(expr)` acts as a flag that its argument expression represents a tag,
    * and is usually removed by the enclosing [[tag]] invocation.
    * At runtime, initializes a naked dynamic [[Tag]] to hold its computed contents.
    * This is the only one of the flag methods besides [[splice]] and [[Frag.apply]] which is expected to usually show up
    * in the final macro-expanded code. The overhead of allocating a few [[Tag]]s here and there is
    * probably negligible. It would be possible to get the [[render]] method to elide
    * the top-level flag methods if appropriate, but that would be a waste of everyone's time.
    */
  def apply (s: String): Tag = new Tag(s)
}

/**
  * The true nature of a frag value. If you can see this as the type of an object at runtime, you're probably
  * not using dottytags right, this is sort of like the singularity of a black hole, it's not supposed to be naked.
  */
final class Frag private[dottytags] (val s: Seq[Element]) { 
  override def toString = s.mkString("") 
}
private[dottytags] object Frag {
  /**
    * At compile-time, acts as a flag that its sequence of argument expression represents a frag,
    * and is sometimes removed by the enclosing [[tag]] invocation.
    * At runtime, initializes a naked dynamic [[Frag]] to hold its computed contents.
    * This is the only one of the sticky methods besides [[splice]] and [[Tag.apply]] which is expected to usually show up
    * in the final macro-expanded code. The overhead of allocating a few [[Frag]]s here and there is
    * probably negligible.
    */
  def apply (s: Seq[Element]): Frag = new Frag(s)
}



// === ADT Groupings, including String === //

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
  * Something [[String]]y (see [[render]]) that represents, in and of itself, a coherent
  * component of an XML tree. [[TagClass!]]es, [[AttrClass!]]es, and [[StyleClass!]]es do not
  * make the cut because while they are valid names for XML objects, they cannot meaningfully exist
  * on their own among their peers. We use types instead of trait inheritance (at the moment) because we need to 
  * transparently include [[String]]s in the type hierarchy.
  * @see [[Modifier]]
  * @see [[Element]]
  */
type Entity = Modifier | Element



// === Splicing and Rendering === //

/**
  * At compile-time, acts as a flag that its arguments must be spliced, and is removed if
  * the splice is seamless (no dynamic chunks leftover, everything can be concatenated into one big string literal).
  * At runtime, finalizes the splicing process, concatenating the 
  * compile-time-concatenated static chunks with their dynamic neighbors.
  */
private def splice(parts: String*): String = 
  val sb = new StringBuilder()
  var i = 0
  val len = parts.size
  while i < len do {
    sb.append(parts(i))
    i = i + 1
  }
  sb.toString

/**
  * Splices a sequence of elements together at runtime while escaping
  * all non-raw strings, for rendering dynamic [[Frag]]s.
  */
private def spliceEscape(parts: Seq[Element]): String =
  val sb = new StringBuilder()
  var i = 0
  val len = parts.size
  while i < len do {
    sb.append(parts(i) match
      case s: String => utils.escape(s)
      case e         => renderOutOfLine(e)
    )
    i = i + 1
  }
  sb.toString

/** 
  * Shortcut for [[splice]] invocations in macros.
  * If `parts` only contains one `Expr[String]`, passes it through without splicing.
  */
private def spliced(parts: Expr[String]*) (using Quotes): Expr[String] =  parts match 
                    case    Seq(head: Expr[String]) => head
                    case s: Seq[Expr[String]]       => '{ splice(${BetterVarargs(s)} *) }

/**
  * Actually renders a dynamic [[Entity]] to a [[String]] at runtime, without getting inlined.
  * [[render]] already falls through to this inline for entities whose exact type isn't statically known.
  * @see [[render]]
  */
private def renderOutOfLine(e: Entity): String = e match
  case a: Attr   => a.str
  case s: Style  => s.str
  case r: Raw    => r.str
  case t: Tag    => t.str
  case f: Frag   => splice(f.s.map(renderOutOfLine) *)
  case s: String => s

/** Runtime implementation of [[pxifyDynamic]] */
private def pxifyOutOfLine(s: String): String = if s.endsWith("px") then s else s + "px"

/**
  * "Renders" an [[Entity]] to a [[String]]. Of course, most entities are to a large extent
  * pre-rendered at compile-time if you're using the library right, so this is mostly just
  * a unified way of turning an already-[[String]]y datatype into a proper [[String]]. This
  * will hopefully get inlined to either a call to the relevant getter method or a call to
  * [[splice]], but falls through to a call to [[renderOutOfLine]] when the type of
  * `e` isn't statically known at compile-time. 
  * @see [[renderOutOfLine]]
  */
extension (inline e: Entity) inline def render: String = inline e match
  case a: Attr   => a.str
  case s: Style  => s.str
  case r: Raw    => r.str
  case t: Tag    => t.str
  case f: Frag   => splice(f.s.map(renderOutOfLine) *)
  case s: String => s
  case _         => renderOutOfLine(e)

/**
  * Private convenience method for erroring out during macro expansion.
  * @param error the error message to display
  * @throws [[scala.quoted.runtime.StopMacroExpansion]] always
  */
private[dottytags] def error(error: String)(using Quotes): Nothing = {
  import quotes.reflect.*
  report.error(error)
  throw scala.quoted.runtime.StopMacroExpansion()
}



// === Tag Tree Construction === //

/** 
  * This is pretty much where all the magic happens, everything that can be statically determined gets
  * concatenated at compile-time, then everything gets spliced together with [[splice]], which boils down to building a [[StringBuilder]]
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
  * println(Tag(
  *   splice(
  *     Array(
  *       "<html class=\"foo\" href=\"bar\" style=\"baz: qux;\">quux",
  *       utils.escape(scala.Long.box(System.currentTimeMillis()).toString()), 
  *       "a</html>"
  *     )
  *   )
  * ).str())
  * ```
  * Which, when run, yields:
  * ```html
  * <html class="foo" href="bar" style="baz1: qux; baz2: qux;">quux1608810396295a</html>
  * ```
  */
extension (inline cl: TagClass) inline def apply(inline args: Entity*): Tag = ${ tagMacro('cl)('args) }
private def tagMacro(cl: Expr[TagClass])(args: Expr[Seq[Entity]])(using Quotes): Expr[Tag] = {
  import quotes.reflect.*
  val cls = cl.value.getOrElse(error("Tag class must be static."))
  val sname       = cls.name
  val selfClosing = cls.selfClosing

  val attrs  = LiftedSplice(List(LiftedStatic(s"<$sname ")))
  val styles = LiftedSplice(Nil)
  val body   = LiftedSplice(Nil)
  
  def iter(as: Seq[Expr[Entity]]): Unit = as.foreach{_ match 
    // Static attr/style/tag/raw string:
    case '{Attr($s)}  => attrs .appendAll(LiftedSplice.lift(s))
    case '{Style($s)} => styles.appendAll(LiftedSplice.lift(s))
    case '{Tag($s)}   => body  .appendAll(LiftedSplice.lift(s))
    case '{Raw($s)}   => body  .appendAll(LiftedSplice.lift(s))
    // Static frag, can be iterated over:
    case '{Frag(${BetterVarargs(s)})} => iter(s)
    // Dynamic attr/style/tag/raw string:
    case '{$e: Attr}  => attrs .append(Span.lift('{$e.str}))
    case '{$e: Style} => styles.append(Span.lift('{$e.str}))
    case '{$e: Tag}   => body  .append(Span.lift('{$e.str}))
    case '{$e: Raw}   => body  .append(Span.lift('{$e.str}))
    // Static frag with a dynamic interior, _frag call can be elided by extracting the Expr[Seq[Element]]
    case '{Frag(${e: Expr[Seq[Element]]})} => body.append(LiftedDynamic('{spliceEscape($e)}))
    // Dynamic frag, must be completely spliced (and escaped) at runtime:
    case '{$e: Frag} => body.append(LiftedDynamic('{spliceEscape($e.s)}))
    // Static or dynamic non-raw string (must be escaped):
    case '{$e: String} => body.append(Span.lift(e) match 
      case LiftedStatic(str) => LiftedStatic(escape(str))
      case LiftedDynamic(e)  => LiftedDynamic('{escape($e)}))
    // Whoops!
    case e => error("Error, unable to expand expression:\n" + e.show)
  }

  BetterVarargs.unapply(args).map { varargs => iter(varargs)
    if styles.isEmpty then attrs.stripTrailingSpace
                      else styles.stripTrailingSpace.prepend(LiftedStatic("style=\"")).append(LiftedStatic("\""))
    if body.isEmpty   then body.prepend(LiftedStatic(if selfClosing then " />" else s"></$sname>"))
                      else body.prepend(LiftedStatic(">")).append(LiftedStatic(s"</$sname>"))
    '{Tag(${spliced(attrs.appendAll(styles).appendAll(body).exprs *)})}
  }.getOrElse(error("Error, unable to traverse varargs:\n" + args.show))
}

/**
  * Represents a valid name for a [[Tag]] 
  * (that is, a string literal containing a valid XML tag name).
  * Make your own with [[tag]] or check out [[predefs.tags]] for predefined ones.
  * Additionally, this can carry a boolean payload indicating that this tag is an HTML
  * self-closing tag, like `<br/>`. You can make your own self-closing tags with the overload of [[tag]]
  * that takes an extra boolean argument.
  */
class TagClass (val name: String, val selfClosing: Boolean) {
  override def toString: String = if selfClosing then s"<$name />" else s"<$name></$name>"
}

/**
  * Creates a static [[TagClass!]], suitable for consumption by [[dottytags.Core.apply]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid XML tag name. The resulting [[TagClass!]] is not self-closing,
  * see the other overload for that.
  */
inline def tag(inline name: String): TagClass = ${ tagValidateMacro('name) }
private def tagValidateMacro(name: Expr[String])(using Quotes): Expr[TagClass] = {
  import quotes.reflect.*
  val sname = name.value.getOrElse(error("Tag name must be a string literal."))
  if !isValidTagName(sname) then error(s"Not a valid XML tag name: $name.")
  '{new TagClass(${Expr(sname)}, ${Expr(false)})}
}
/**
  * Creates a static [[TagClass!]], suitable for consumption by [[dottytags.Core.apply]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * if `name` is not a valid XML tag name, or if `sc` is not a boolean literal or other static
  * boolean value. The resulting [[TagClass!]] is self-closing if `sc == true`.
  */
inline def tag(inline name: String, inline sc: Boolean): TagClass = ${ tagValidateMacroSC('name, 'sc) }
private def tagValidateMacroSC(name: Expr[String], sc: Expr[Boolean])(using Quotes): Expr[TagClass] = {
  import quotes.reflect.*
  val sname = name.value.getOrElse(error("Tag name must be a string literal."))
  if !isValidTagName(sname) then error(s"Not a valid XML tag name: $name.")
  val scs  = sc.value.getOrElse(error("Self-closing flag must be a literal."))
  '{new TagClass(${Expr(sname)}, ${Expr(scs)})}
}

given TagClassFromExpr: FromExpr[TagClass] with {
  def unapply(x: Expr[TagClass])(using Quotes): Option[TagClass] = x match
    case '{ new TagClass(${Expr(name: String)}, ${Expr(b)}: Boolean) } => Some(new TagClass(name, b))
}
/** To allow for tags to be specified with just a name and no body to make them empty. */
inline implicit def tagClass2EmptyTag(inline cl: TagClass): Tag = cl()



// === Raw Text Input === //

/** The simplest macro, just tags `raw` with [[Raw]] */
inline def raw(inline raw: String): Raw = ${ rawMacro('raw) }
private def rawMacro(raw: Expr[String])(using Quotes): Expr[Raw] = '{Raw($raw)}



// === Arbitrary Inputs (Fragments) === //

/** 
  * Binds up the given elements, escaping any non-[[Raw]] strings pre-emptively.
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
  case BetterVarargs(as) => '{Frag(${BetterVarargs(as)})}
  case e: Expr[Seq[Element]] => '{Frag($e)}
  case e => error("Error, unable to traverse or expand:\n" + e.show)
private def fragMacroVals(elems: Expr[Seq[AnyVal]])(using Quotes): Expr[Frag] = elems match 
  case BetterVarargs(as) => '{Frag(${BetterVarargs(as.map(a => '{$a.toString}))})}
  case e: Expr[Seq[AnyVal]] => '{Frag($e.map(_.toString))}
  case e => error("Error, unable to traverse or expand:\n" + e.show)



// === HTML/HTML/SVG Attributes === //

/**
  * Represents a valid name for an [[Attr]] 
  * (that is, a string literal containing a valid XML attribute name).
  * Make your own with [[attr]] or check out [[predefs.attrs]] for predefined ones.
  * Unlike [[StyleClass!]] and [[TagClass!]],
  * this does not carry any extra configuration payload.
  */ 
final class AttrClass (val name: String, val raw: Boolean) {
    override def toString: String = name
}

/**
  * Creates a static [[AttrClass!]], suitable for consumption by [[dottytags.Core.:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid XML attribute name.
  */ 
inline def attr(inline name: String): AttrClass = ${ attrValidateMacro('name, 'false) }
inline def attrRaw(inline name: String): AttrClass = ${ attrValidateMacro('name, 'true) }
private def attrValidateMacro(name: Expr[String], raw: Expr[Boolean])(using Quotes): Expr[AttrClass] =
  import quotes.reflect.*
  val sname = name.value.getOrElse(error("Attr name must be a string literal."))
  val sraw =    raw.value.getOrElse(error("Raw flag must be a boolean literal"))
  if !isValidAttrName(sname) && !sraw then error(s"Not a valid XML attribute name: $name.")
  '{new AttrClass(${Expr(sname)}, ${Expr(sraw)})}

given AttrClassFromExpr: FromExpr[AttrClass] with
  def unapply(x: Expr[AttrClass])(using Quotes): Option[AttrClass] = x match
    case '{ new AttrClass(${Expr(name: String)}, ${Expr(raw: Boolean)})} => Some(new AttrClass(name, raw))
    case _ => None

/** 
  * Just checks `attr` is static, escapes `value`, and then splices together the attribute string.
  * If we move to a less cursed system using actual case classes or whatever then this can become a member method of [[AttrClass!]].
  */
extension (inline attr: AttrClass) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ attrMacro('attr, 'value) }
private def attrMacro(attr: Expr[AttrClass], setTo: Expr[String])(using Quotes): Expr[Attr] = attr.value.map { cls => setTo.value match 
  case Some(setTo: String) => '{Attr(${        Expr(cls.name + "=\""   + escape(setTo)       + "\" ")})}
  case _                   => '{Attr(${spliced(Expr(cls.name + "=\""), '{escape($setTo)}, Expr("\" "))})}
}.getOrElse(error("Attribute class must be static."))

extension (inline attr: AttrClass) inline def empty: Attr = ${ emptyAttrMacro('attr) }
private def emptyAttrMacro(attr: Expr[AttrClass])(using Quotes): Expr[Attr] = attr.value.map { cls => 
  '{Attr(${ Expr(cls.name + "=\"" + escape(cls.name) + "\" ")})}
}.getOrElse(error("Attribute class must be static."))



// === CSS Styles === //

/**
  * Represents a valid name for a [[Style]] 
  * (that is, a string literal containing a valid CSS style name).
  * Make your own with [[css]] or [[cssPx]] or check out [[predefs.styles]] for predefined ones.
  * Additionally, this can carry a boolean payload indicating that this style takes an argument
  * with a 'px' suffix, which will be automatically added if not present in your argument.
  * [[css]] makes non-px [[StyleClass!]]es, [[cssPx]] makes px ones.
  */
class StyleClass (val name: String, val px: Boolean) {
  
}

/**
  * Creates a static [[StyleClass!]], suitable for consumption by [[:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid CSS style name. The resulting [[StyleClass!]] is not px-suffixed,
  * see [[cssPx]] for that.
  */
inline def css(inline name: String): StyleClass = ${ cssValidateMacro('name, '{false}) }
/**
  * Creates a static [[StyleClass!]], suitable for consumption by [[:=]].
  * Macro expansion will fail if `name` is not a string literal or other static string value,
  * or if `name` is not a valid CSS style name. The resulting [[TagClass!]] is px-suffixed,
  * see [[css]] for a non-suffixed class.
  */
inline def cssPx(inline name: String): StyleClass = ${ cssValidateMacro('name, '{true}) }
private def cssValidateMacro(name: Expr[String], px: Expr[Boolean])(using Quotes): Expr[StyleClass] = {
  import quotes.reflect.*
  val sname = name.value.getOrElse(error("Style name must be a string literal."))
  val spx   = px.value.getOrElse(error("Pixel style must be a boolean literal."))

  if !isValidStyleName(sname) then error(s"Not a valid CSS style name: $name.")
  '{new StyleClass(${Expr(sname)}, ${Expr(spx)})}
}

/**
  * Just checks `style` is static, escapes `value` (dynamic or static), 
  * adds the px suffix (dynamically or statically) if `style` says so 
  * and then splices together the style string.
  * If we move to a less cursed system using actual case classes or whatever then this can become a member method of [[StyleClass!]].
  */
extension (inline style: StyleClass) @targetName("setStyle") inline def :=(inline value: String): Style = ${ styleMacro('style, 'value) }
private def styleMacro(style: Expr[StyleClass], setTo: Expr[String])(using Quotes): Expr[Style] = style.value.map { cls => setTo.value match
  case Some(setTo: String) => '{Style(${Expr(cls.name + ": " + pxifyStatic(escape(setTo), cls.px) + "; ")})}
  case _ => '{Style(${spliced(Expr(cls.name + ": "), pxifyDynamic('{escape($setTo)}, cls.px), Expr("; "))})}
}.getOrElse(error("Style class must be static."))
/** Pxifies a static string: if it doesn't already end with 'px', add it */
private def pxifyStatic (s: String, px: Boolean): String  = if px && !s.endsWith("px") then s + "px" else s
/** Pxifies a dynamic string: if, at runtime, its computed value doesn't already end with 'px', add it. */
private def pxifyDynamic(s: Expr[String], px: Boolean)(using Quotes): Expr[String] = if px then '{ pxifyOutOfLine($s) } else s

given StyleClassFromExpr: FromExpr[StyleClass] with {
  def unapply(x: Expr[StyleClass])(using Quotes): Option[StyleClass] = x match
    case '{ new StyleClass(${Expr(name: String)}, ${Expr(spx: Boolean)})} => Some(new StyleClass(name, spx))
}



// === Random Test Functions === //

inline def showCode(inline a: Any): Unit = ${ showCodeMacro('a) }
private def showCodeMacro(a: Expr[Any])(using Quotes): Expr[Unit] = {
  import quotes.reflect.*
  '{
    println(s"Code for ${${Expr(Position.ofMacroExpansion.startLine.toString)}}:")
    println(${Expr(Printer.TreeAnsiCode.show(a.asTerm))})
    ()
  }
}

inline def showTree(inline a: Any): Unit = ${ showTreeMacro('a) }
private def showTreeMacro(a: Expr[Any])(using Quotes): Expr[Unit] = {
  import quotes.reflect.*
  '{
    println(s"Tree for ${${Expr(Position.ofMacroExpansion.startLine.toString)}}:")
    println(${Expr(Printer.TreeStructure.show(a.asTerm))})
    ()
  }
}