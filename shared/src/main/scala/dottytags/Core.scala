package dottytags

import scala.quoted._
import scala.annotation.targetName
import scala.language.implicitConversions

object Core {

  /** 
    * "Sticky Note" methods that are inserted into the macro expansion as short-lived tags
    * which do not make it into the final output and which should not be used by the calling
    * code (hence the mangled names)
    */
  object _sticky {
    def _splice(parts: String*): String = parts.mkString("")
    def _attrName   (s: String): AttrName               = s
    def _attr       (s: String): Attr                   = s
    def _styleName  (s: String, px: Boolean): StyleName = s
    def _style      (s: String): Style                  = s
    def _raw        (s: String): Raw                    = s
    def _tagName    (s: String, sc: Boolean): TagName   = s
    def _tag        (s: String): Tag                    = s
    implicit def _frag (s: Seq[Modifier]): Frag         = s.mkString("")
    opaque type AttrName  = String
    opaque type Attr      = String
    opaque type StyleName = String
    opaque type Style     = String
    opaque type Raw       = String
    opaque type TagName   = String
    opaque type Tag       = String
    opaque type Frag      = String
    type Modifier         = String | Attr | Style | Raw | Tag | Frag
  }

  export _sticky.{AttrName, Attr, StyleName, Style, Raw, TagName, Tag, Frag, Modifier, given}
  
  private trait Span
  private case class Static(str: String) extends Span
  private case class Dynamic(expr: Expr[String]) extends Span
  private case class Splice(parts: Seq[Span]) extends Span {
    def flatten: Splice = Splice(parts.flatMap(_ match
      case s: Static => Seq(s)
      case d: Dynamic => Seq(d)
      case sp: Splice => sp.flatten.parts
      ))
    def +(s: Span): Splice = Splice(s match
      case Static(s) => parts.headOption match 
        case Some(Static(s1)) => Static(s1 + s) +: parts 
        case Some(Dynamic(_)) => Static(s) +: parts
        case _ => Seq(Static(s))
      case d @ Dynamic(_) => d +: parts
      case Splice(ps) => ps.foldLeft(this)((acc, p) => acc + p).parts
    )
    def exprs(using Quotes): Seq[Expr[String]] = parts.collect {
      case Static(str) => Expr(str)
      case Dynamic(expr) => expr
    }
  }
  private def unstick_string(s: Expr[String])(using Quotes): Span = s match 
    case Expr(s: String) => Static(s)
    case '{ _sticky._splice(${BetterVarargs(parts)}: _*) } => Splice(parts.map(unstick_string))
    case dyn: Expr[String] => Dynamic(dyn)
  private def unstick_attrName(s: Expr[AttrName])(using Quotes): Static = s match 
    case '{ _sticky._attrName(${Expr(s: String)}) } => Static(s)
    case _ => error("Illegally-obtained AttrName value")
  private def unstick_attr(s: Expr[Attr])(using Quotes): Span = s match 
    case '{ _sticky._attr($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Attr value")
  private def unstick_styleName(s: Expr[StyleName])(using Quotes): (Static, Boolean) = s match 
    case '{ _sticky._styleName(${Expr(s: String)}, ${Expr(b: Boolean)}) } => (Static(s), b)
    case _ => error("Illegally-obtained StyleName value")
  private def unstick_style(s: Expr[Style])(using Quotes): Span = s match 
    case '{ _sticky._style($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Style value")
  private def unstick_raw(s: Expr[Raw])(using Quotes): Span = s match 
    case '{ _sticky._raw($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Raw value")
  private def unstick_tagName(s: Expr[TagName])(using Quotes): (Static, Boolean) = s match 
    case '{ _sticky._tagName(${Expr(s: String)}, ${Expr(b: Boolean)}) } => (Static(s), b)
    case _ => error("Illegally-obtained TagName value")
  private def unstick_tag(s: Expr[Tag])(using Quotes): Span = s match 
    case '{ _sticky._tag($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Tag value")
  private def unstick_frag(s: Expr[Frag])(using Quotes): Seq[Expr[Modifier]] = s match 
    case '{ _sticky._frag(Seq[Modifier](${BetterVarargs(parts)}: _*)) } => parts
    case _ => error("Illegally-obtained Tag value")

  private def stick_splice(parts: Expr[String]*)(using Quotes): Expr[String]  = '{ _sticky._splice(${Varargs(parts)}: _*) }
  private def stick_attrName (s: Expr[String])                   (using Quotes): Expr[AttrName]  = '{ _sticky._attrName($s) }
  private def stick_attr     (s: Expr[String])                   (using Quotes): Expr[Attr]      = '{ _sticky._attr($s) }
  private def stick_styleName(s: Expr[String], px: Expr[Boolean])(using Quotes): Expr[StyleName] = '{ _sticky._styleName($s, $px) }
  private def stick_style    (s: Expr[String])                   (using Quotes): Expr[Style]     = '{ _sticky._style($s) }
  private def stick_raw      (s: Expr[String])                   (using Quotes): Expr[Raw]       = '{ _sticky._raw($s) }
  private def stick_tagName  (s: Expr[String], sc: Expr[Boolean])(using Quotes): Expr[TagName]   = '{ _sticky._tagName($s, $sc) }
  private def stick_tag      (s: Expr[String])                   (using Quotes): Expr[Tag]       = '{ _sticky._tag($s) }

  private implicit def str(str: String)(using Quotes): Expr[String] = Expr(str)

  private def pprint(obj: Any, depth: Int = 0, paramName: Option[String] = None): Unit = {
    val indent = "  " * depth
    val prettyName = paramName.fold("")(x => s"$x: ")
    val ptype = obj match { case _: Iterable[Any] => "" case obj: Product => obj.productPrefix case _ => obj.toString }

    println(s"$indent$prettyName$ptype")

    obj match {
      case seq: Iterable[Any] => seq.foreach(pprint(_, depth + 1))
      case obj: Product => (obj.productIterator zip obj.productElementNames)
        .foreach { case (subObj, paramName) => pprint(subObj, depth + 1, Some(paramName)) }
      case _ =>
    }
  }

  private def error(error: String)(using Quotes): Nothing = 
    import quotes.reflect._
    report.error(error)
    throw scala.quoted.runtime.StopMacroExpansion()

  inline def print(inline s: Any): Any = ${ printMacro('s) }
  private def printMacro(s: Expr[Any])(using Quotes): Expr[Any] =
    import quotes.reflect._
    pprint(s.asTerm)
    s

  inline def tag(inline name: String): TagName = ${ tagNameMacro('name, '{false}) }
  inline def tagSelfClosing(inline name: String): TagName = ${ tagNameMacro('name, '{true}) }
  private def tagNameMacro(name: Expr[String], sc: Expr[Boolean])(using Quotes): Expr[TagName] = name.value match 
    case Some(name: String) => if isValidTagName(name) then stick_tagName(name, sc) else 
              error(s"Not a valid XML tag name: $name.")
    case _ => error("Tag name must be a literal string.")

  extension (inline name: TagName) inline def apply(inline args: Modifier*): Tag = ${ tagMacro('name)('args) }
  private def tagMacro(name: Expr[TagName])(args: Expr[Seq[Modifier]])(using Quotes): Expr[Tag] = 
    import quotes.reflect._
    val (nstr, selfClosing) = unstick_tagName(name)
    args match
      case BetterVarargs(args) => 
        var attrsSplice = Splice(Seq(Static(s"<$nstr ")))
        var stylesSplice = Splice(Seq())
        var bodySplice = Splice(Seq())
        def iter(args: Seq[Expr[Modifier]]): Unit = {
          args.foreach(a =>
            if a.isExprOf[Attr] then attrsSplice = attrsSplice + unstick_attr(a.asExprOf[Attr])
            else if a.isExprOf[Style] then stylesSplice = stylesSplice + unstick_style(a.asExprOf[Style])
            else if a.isExprOf[String] then bodySplice = bodySplice + unstick_string(a.asExprOf[String])
            else if a.isExprOf[Raw] then bodySplice = bodySplice + unstick_raw(a.asExprOf[Raw])
            else if a.isExprOf[Tag] then bodySplice = bodySplice + unstick_tag(a.asExprOf[Tag])
            else if a.isExprOf[Frag] then iter(unstick_frag(a.asExprOf[Frag])))
        }
        iter(args)
        stick_tag(stick_splice((attrsSplice + stylesSplice + bodySplice).exprs: _*))
      case _ => error("Varargs was somehow not varargs")
                
  inline def raw(inline raw: String): Raw = ${ rawMacro('raw) }
  private def rawMacro(raw: Expr[String])(using Quotes): Expr[Raw] = stick_raw(raw)

  inline def attr(inline name: String): AttrName = ${ attrNameMacro('name) }
  private def attrNameMacro(name: Expr[String])(using Quotes): Expr[AttrName] = name.value match 
    case Some(name: String) => if isValidAttrName(name) then stick_attrName(name) else 
              error(s"Not a valid XML attribute name: $name.")
    case _ => error("Attribute name must be a literal string.")

  inline def css  (inline name: String): StyleName = ${ styleNameMacro('name, '{false}) }
  inline def cssPx(inline name: String): StyleName = ${ styleNameMacro('name, '{true} ) }
  private def styleNameMacro(name: Expr[String], px: Expr[Boolean])(using Quotes): Expr[StyleName] = name.value match 
    case Some(name: String) => if isValidStyleName(name) then stick_styleName(name, px) else 
              error(s"Not a valid CSS style name: $name.")
    case _ => error("Style name must be a literal string.")
  
  extension (inline name: AttrName) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ attrMacro('name, 'value) }
  private def attrMacro(name: Expr[AttrName], setTo: Expr[String])(using Quotes): Expr[Attr] = {
    val n = unstick_attrName(name)
    setTo.value match 
      case Some(setTo: String) => stick_attr(n.str + "=\"" + escape(setTo) + "\"")
      case _ => stick_attr(stick_splice(n.str + "=\"", '{escape($setTo)}, "\""))
  }
  
  extension (inline name: StyleName) @targetName("setStyle") inline def :=(inline value: String): Style = ${ styleMacro('name, 'value) }
  private def styleMacro(name: Expr[StyleName], setTo: Expr[String])(using Quotes): Expr[Style] = {
    val (n, doPx) = unstick_styleName(name)
    setTo.value match 
      case Some(setTo: String) => stick_style(n.str + ": " + escape(setTo) + (if doPx then "px;" else ";"))
      case _ => stick_style(stick_splice(n.str + ": ", '{escape($setTo)}, if doPx then "px;" else ";"))
  }

}