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
    def _attr       (s: String): Attr                   = new Attr(s)
    def _style      (s: String): Style                  = new Style(s)
    def _raw        (s: String): Raw                    = new Raw(s)
    def _tag        (s: String): Tag                    = new Tag(s)
    def _frag (s: Seq[Element]): Frag                   = new Frag(s)
    final class Attr private[_sticky] (val str: String) {
      override def toString = str
    }
    final class Style private[_sticky] (val str: String) {
      override def toString = str
    }
    final class Raw private[_sticky] (val str: String) {
      override def toString = str
    }
    final class Tag private[_sticky] (val str: String) {
      override def toString = str
    }
    final class Frag private[_sticky] (val s: Seq[Element]) {
      override def toString = s.mkString("")
    }
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
    def concat(s: Splice): Splice = s.parts.foldLeft(this)((acc, p) => acc.append(p))
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
    case '{ _sticky._splice($part) } => Splice(Seq(unstick_string(part)))
    case e => error(e.show)
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

  private def stick_splice(parts: Expr[String]*)(using Quotes): Expr[String]  = 
    if parts.length == 1 then parts.head
    else '{ _sticky._splice(${Varargs(parts)}: _*) }
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

  
  extension (inline cl: TagClass) inline def apply(inline args: Entity*): Tag = ${ tagMacro('cl)('args) }
  //extension (inline cl: TagClass) @targetName("elemsOnly") inline def apply(inline elems: Element*): Tag = ${ tagMacro('cl)('{Seq()})('elems) }
  //extension (inline cl: TagClass) @targetName("modsAndElems") inline def apply(inline mods: Modifier*)(inline elems: Element*): Tag = ${ tagMacro('cl)('mods)('elems) }
  private def tagMacro(cl: Expr[TagClass])(args: Expr[Seq[Entity]])(using Quotes): Expr[Tag] = 
    import quotes.reflect._
    val cls = cl.value.getOrElse(error("Tag class must be static."))
    val sname = cls.name
    val selfClosing = cls.selfClosing
    var attrsSplice = Splice(List(Static(s"<$sname ")))
    var stylesSplice = Splice(Nil)
    var bodySplice = Splice(Nil)
    args match
      case BetterVarargs(args) => 
        def iter(as: Seq[Expr[Element]], careAboutEscaping: Boolean = true): Unit = as.foreach(_ match 
          case '{_sticky._raw(_sticky._splice($s))}  => bodySplice  = bodySplice.concat(unstick_splice(s))
          case '{_sticky._raw($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
          case '{_sticky._tag(_sticky._splice($s))}   => bodySplice   = bodySplice.concat(unstick_splice(s))
          case '{_sticky._tag($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
          case '{_sticky._frag(${BetterVarargs(s)})} => iter(s, careAboutEscaping = false)
          case '{_sticky._frag(${e: Expr[Seq[Element]]})} => bodySplice = bodySplice.append(Dynamic('{($e.mkString(""))}))
          case '{$e: Tag} => bodySplice = bodySplice.append(Dynamic('{$e.str}))
          case '{$e: Raw} => bodySplice = bodySplice.append(Dynamic('{$e.str}))
          case '{$e: String} => if careAboutEscaping then bodySplice = bodySplice.append(unstick_string(e) match 
            case Static(str) => Static(escape(str))
            case Dynamic(e) => Dynamic('{escape($e)})) else bodySplice = bodySplice.append(unstick_string(e))
          case e => error("Error: " + e.show)
        )
        args.foreach(_ match 
            case '{_sticky._attr(_sticky._splice($s))}  => attrsSplice  = attrsSplice.concat(unstick_splice(s))
            case '{_sticky._attr($s)}  => attrsSplice  = attrsSplice.append(unstick_string(s))
            case '{_sticky._style(_sticky._splice($s))} => stylesSplice = stylesSplice.concat(unstick_splice(s))
            case '{_sticky._style($s)} => stylesSplice = stylesSplice.append(unstick_string(s))
            case '{_sticky._raw(_sticky._splice($s))}  => bodySplice  = bodySplice.concat(unstick_splice(s))
            case '{_sticky._raw($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
            case '{_sticky._tag(_sticky._splice($s))}   => bodySplice   = bodySplice.concat(unstick_splice(s))
            case '{_sticky._tag($s)}   => bodySplice   = bodySplice.append(unstick_string(s))
            case '{_sticky._frag(${BetterVarargs(s)})} => iter(s, careAboutEscaping = false)
            case '{_sticky._frag(${e: Expr[Seq[Element]]})} => bodySplice = bodySplice.append(Dynamic('{($e.mkString(""))}))
            case '{$e: Tag} => bodySplice = bodySplice.append(Dynamic('{$e.str}))
            case '{$e: Raw} => bodySplice = bodySplice.append(Dynamic('{$e.str}))
            case '{$e: String} => bodySplice = bodySplice.append(unstick_string(e) match 
              case Static(str) => Static(escape(str))
              case Dynamic(e) => Dynamic('{escape($e)}))
            case e => error("Error: " + e.show))
        if stylesSplice.parts.nonEmpty then stylesSplice = (stylesSplice.prepend(Static("style=\""))).stripTrailingSpace.append(Static("\""))
        if attrsSplice.parts.nonEmpty && stylesSplice.parts.isEmpty then attrsSplice = attrsSplice.stripTrailingSpace
        if bodySplice.parts.isEmpty then bodySplice = bodySplice.append(Static(if selfClosing then "/>" else s"></$sname>"))
        else bodySplice = bodySplice.append(Static(s"</$sname>"))
             stylesSplice = stylesSplice.append(Static(">"))
        stick_tag(stick_splice((Splice(Seq()).concat(attrsSplice).concat(stylesSplice).concat(bodySplice)).exprs: _*))
      case _ => error("Varargs was somehow not varargs")
                
  inline def raw(inline raw: String): Raw = ${ rawMacro('raw) }
  private def rawMacro(raw: Expr[String])(using Quotes): Expr[Raw] = stick_raw(raw)

  //implicit line def seq2frag[T <: Core.Element](seq: Seq[T]): Core.Element = Core.frag(seq)
  inline def frag(inline elems: Element*): Frag = ${ fragMacro('elems) }
  inline def bind(inline elem: Raw | Frag | Tag): Frag = ${ bindMacro('elem) }
  inline def bind(inline elems: Seq[Raw | Frag | Tag]): Frag = ${ bindMacroSeq('elems) }
  private def fragMacro(elems: Expr[Seq[Element]])(using Quotes): Expr[Frag] = elems match 
    case BetterVarargs(as) => stick_frag(BetterVarargs(as.map(a => a match 
      case '{$e: Raw}  => a
      case '{$e: Frag} => a
      case '{$e: Tag}  => a
      case '{$e: String} => unstick_string(e) match 
        case Static(str) => Expr(escape(str))
        case Dynamic(e) => '{escape($e)}
      case e => error("The dynamic expression \"" + e.show + "\" needs to be bound with bind() before you can use it.")
    )))
  private def bindMacro(elem: Expr[Raw | Frag | Tag])(using Quotes): Expr[Frag] = stick_frag(BetterVarargs(Seq(elem)))
  private def bindMacroSeq(elems: Expr[Seq[Raw | Frag | Tag]])(using Quotes): Expr[Frag] = stick_frag(elems)
    
  
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