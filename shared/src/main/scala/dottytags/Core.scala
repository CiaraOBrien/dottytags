package dottytags

import scala.quoted._
import scala.annotation.targetName
import scala.unchecked
import scala.language.implicitConversions
import scala.collection.mutable.ArrayBuffer

object Core {

  /** 
    * "Sticky Note" methods that are inserted into the macro expansion as short-lived tags
    * which do not make it into the final output and which should not be used by the calling
    * code (hence the mangled names)
    */
  object _sticky {
    def _splice(parts: String*): String = parts.mkString("")
    def _attr       (s: String): Attr                   = s
    def _style      (s: String): Style                  = s
    def _raw        (s: String): Raw                    = s
    def _tag        (s: String): Tag                    = s
    def _frag (s: Seq[Element]): Frag                   = s.mkString("")
    opaque type Attr       = String
    opaque type Style      = String
    opaque type Raw        = String
    opaque type Tag        = String
    opaque type Frag       = String
    type Modifier = Attr | Style
    type Entity = Modifier | Element
    type Element = Raw   | Tag | Frag | String
  }

  export _sticky.{Attr, Style, Raw, Tag, Frag, Entity, Element, Modifier}
  export TagClass.{_, given}
  export AttrClass.{_, given}
  export StyleClass.{_, given}

  private trait Span
  private case class Static(str: String) extends Span
  private case class Dynamic(expr: Expr[String]) extends Span
  private class Splice(val parts: Seq[Span]) {
    def append(s: Span): Splice = Splice(s match
      case Static(s) => parts.lastOption match 
        case Some(Static(s1)) => parts.updated(parts.size - 1, Static(s1 + s))
        case _                => parts.appended(Static(s))
      case d: Dynamic         => parts.appended(d))
    
    def prepend(s: Span): Splice = Splice(s match
      case Static(s) => parts.headOption match 
        case Some(Static(s1)) => parts.updated(0, Static(s + s1))
        case _                => parts.prepended(Static(s))
      case d: Dynamic         => parts.prepended(d)
    )
    def concat(s: Splice): Splice = Splice(this.parts.concat(s.parts))
    def exprs(using Quotes): Seq[Expr[String]] = parts.collect {
      case Static(str) => Expr(str)
      case Dynamic(expr) => expr
    }
    def stripTrailingSpace: Splice = Splice(parts.lastOption match
      case Some(Static(s)) => parts.updated(parts.size - 1, Static(s.stripSuffix(" ")))
      case _ => parts
    )
  }
  private def unstick_splice(s: Expr[String])(using Quotes): Splice = s match 
    case '{ _sticky._splice(${BetterVarargs(parts)}: _*) } => Splice(parts.map(unstick_string))
  private def unstick_string(s: Expr[String])(using Quotes): Span = s match 
    case Expr(s: String) => Static(s)
    case dyn: Expr[String] => Dynamic(dyn)
  private def unstick_attr(s: Expr[Attr])(using Quotes): Span = s match 
    case '{ _sticky._attr($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Attr value")
  private def unstick_style(s: Expr[Style])(using Quotes): Span = s match 
    case '{ _sticky._style($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Style value")
  private def unstick_raw(s: Expr[Raw])(using Quotes): Span = s match 
    case '{ _sticky._raw($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Raw value")
    case _ => error("Illegally-obtained Text value")
  private def unstick_tag(s: Expr[Tag])(using Quotes): Span = s match 
    case '{ _sticky._tag($s) } => unstick_string(s)
    case _ => error("Illegally-obtained Tag value")
  private def unstick_frag(s: Expr[Frag])(using Quotes): Seq[Expr[Element]] = s match 
    case '{ _sticky._frag(Seq[Element](${BetterVarargs(parts)}: _*)) } => parts
    case _ => error("Illegally-obtained Frag value")

  private def stick_splice(parts: Expr[String]*)(using Quotes): Expr[String]  = '{ _sticky._splice(${Varargs(parts)}: _*) }
  private def stick_attr     (s: Expr[String])                   (using Quotes): Expr[Attr]      = '{ _sticky._attr($s) }
  private def stick_style    (s: Expr[String])                   (using Quotes): Expr[Style]     = '{ _sticky._style($s) }
  private def stick_raw      (s: Expr[String])                   (using Quotes): Expr[Raw]       = '{ _sticky._raw($s) }
  private def stick_tag      (s: Expr[String])                   (using Quotes): Expr[Tag]       = '{ _sticky._tag($s) }
  private def stick_frag     (s: Expr[Seq[Element]])             (using Quotes): Expr[Frag]      = '{ _sticky._frag($s) }

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

  inline def print(inline s: Any): Any = ${ printMacro('s) }
  private def printMacro(s: Expr[Any])(using Quotes): Expr[Any] =
    import quotes.reflect._
    pprint(s.asTerm)
    s

  extension (inline cl: TagClass) inline def apply(inline mods: Modifier*)(inline elems: Element*): Tag = ${ tagMacro('cl)('mods)('elems) }
  private def tagMacro(cl: Expr[TagClass])(mods: Expr[Seq[Modifier]])(elems: Expr[Seq[Element]])(using Quotes): Expr[Tag] = 
    import quotes.reflect._
    val cls = cl.value.getOrElse(error("Tag class must be static."))
    val sname = cls.name
    val selfClosing = cls.selfClosing
    var attrsSplice = Splice(List(Static(s"<$sname ")))
    var stylesSplice = Splice(Nil)
    var bodySplice = Splice(Nil)
    mods match
      case BetterVarargs(args) => 
        args.foreach(_ match 
            case '{_sticky._attr(_sticky._splice($s))}  => attrsSplice  = attrsSplice.concat(unstick_splice(s))
            case '{_sticky._attr($s)}  => attrsSplice  = attrsSplice.append(unstick_string(s))
            case '{_sticky._style(_sticky._splice($s))} => stylesSplice = stylesSplice.concat(unstick_splice(s))
            case '{_sticky._style($s)} => stylesSplice = stylesSplice.append(unstick_string(s))
            case e => error("Error: " + e.show))
        if stylesSplice.parts.nonEmpty then stylesSplice = (stylesSplice.prepend(Static("style=\""))).stripTrailingSpace.append(Static("\""))
      case _ => error("Varargs was somehow not varargs")
      if attrsSplice.parts.nonEmpty && stylesSplice.parts.isEmpty then attrsSplice = attrsSplice.stripTrailingSpace
    elems match 
      case BetterVarargs(args) =>
        def iter(as: Seq[Expr[Element]]): Unit = as.foreach(_ match 
            case '{_sticky._raw(_sticky._splice($s))}  => bodySplice  = bodySplice.concat(unstick_splice(s))
            case '{_sticky._raw($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
            case '{_sticky._tag(_sticky._splice($s))}   => bodySplice   = bodySplice.concat(unstick_splice(s))
            case '{_sticky._tag($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
            case '{_sticky._frag(${BetterVarargs(s)})} => iter(s)
            case '{_sticky._frag(${e: Expr[Seq[Element]]})} => bodySplice = bodySplice.append(Dynamic('{($e.mkString(""))}))
            case e: Expr[String] => bodySplice = bodySplice.append(unstick_string(e) match 
              case Static(str) => Static(escape(str))
              case Dynamic(e) => Dynamic('{escape($e)})
            )
            case e => error("Error: " + e.show))
        iter(args)
        if bodySplice.parts.isEmpty then bodySplice = bodySplice.append(Static(if selfClosing then "/>" else s"></$sname>"))
        else bodySplice = bodySplice.append(Static(s"</$sname>"))
             stylesSplice = stylesSplice.append(Static(">"))
        stick_tag(stick_splice((Splice(Seq()).concat(attrsSplice).concat(stylesSplice).concat(bodySplice)).exprs: _*))
                
  inline def raw(inline raw: String): Raw = ${ rawMacro('raw) }
  private def rawMacro(raw: Expr[String])(using Quotes): Expr[Raw] = stick_raw(raw)

  inline def frag(inline elems: Element*): Frag = ${ fragMacro('elems) }
  private def fragMacro(elems: Expr[Seq[Element]])(using Quotes): Expr[Frag] = elems match 
    case BetterVarargs(as) => stick_frag(BetterVarargs(as.map(a => a match 
      case '{_sticky._raw($_)}  => a
      case '{_sticky._tag($_)}  => a
      case '{_sticky._frag($_)} => a
      case e: Expr[String] => unstick_string(e) match 
        case Static(str) => Expr(escape(str))
        case Dynamic(e) =>'{escape($e)}
      )))
  
  extension (inline attr: AttrClass) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ attrMacro('attr, 'value) }
  private def attrMacro(attr: Expr[AttrClass], setTo: Expr[String])(using Quotes): Expr[Attr] = {
    val cls = attr.value.getOrElse(error("Attr class must be static."))
    val sname = cls.name
    setTo.value match 
      case Some(setTo: String) => stick_attr(sname + "=\"" + escape(setTo) + "\" ")
      case _ => stick_attr(stick_splice(sname + "=\"", '{escape($setTo)}, "\" "))
  }
  
  extension (inline style: StyleClass) @targetName("setStyle") inline def :=(inline value: String): Style = ${ styleMacro('style, 'value) }
  private def styleMacro(style: Expr[StyleClass], setTo: Expr[String])(using Quotes): Expr[Style] = {
    val cls = style.value.getOrElse(error("Style class must be static."))
    val sname = cls.name
    setTo.value match 
      case Some(setTo: String) => stick_style(sname + ": " + escape(setTo) + "; ")
      case _ => stick_style(stick_splice(sname + ": ", '{escape($setTo)}, "; "))
  }

}