package dottytags

import scala.quoted._
import scala.annotation.targetName

object Core {

  /** 
    * "Sticky Note" methods that are inserted into the macro expansion as short-lived tags
    * which do not make it into the final output and which should not be used by the calling
    * code (hence the mangled names)
    */
  /*object _sticky {
    def _splice(parts: String*): String = parts.mkString("")
    def _attrName   (s: String): AttrName  = s
    def _attr       (s: String): Attr      = s
    def _styleName  (s: String): StyleName = s
    def _styleNamePx(s: String): StyleName = s
    def _style      (s: String): Style     = s
    def _unescaped  (s: String): Unescaped = s
    opaque type AttrName  = String
    opaque type Attr      = String
    opaque type StyleName = String
    opaque type Style     = String
    opaque type Unescaped = String
  }

  export _sticky.{AttrName, Attr, StyleName, Style, Unescaped}

  private[dottytags] def stick_splice(parts: Expr[String]*)(using Quotes): Expr[String] = 
    import quotes.reflect._
    '{ _sticky._splice(${Varargs(parts)}: _*) }
    //Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._splice")), List(Term.of(Varargs(parts)))).asExprOf[String]
  private[dottytags] def stick_attrName(s: Expr[String])(using Quotes): Expr[AttrName] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._attrName")), List(Term.of(s))).asExprOf[AttrName]
  private[dottytags] def stick_attr(s: Expr[String])(using Quotes): Expr[Attr] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._attr")), List(Term.of(s))).asExprOf[Attr]
  private[dottytags] def stick_styleName(s: Expr[String])(using Quotes): Expr[StyleName] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._styleName")), List(Term.of(s))).asExprOf[StyleName]
  private[dottytags] def stick_styleNamePx(s: Expr[String])(using Quotes): Expr[StyleName] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._styleNamePx")), List(Term.of(s))).asExprOf[StyleName]
  private[dottytags] def stick_style(s: Expr[String])(using Quotes): Expr[Style] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._style")), List(Term.of(s))).asExprOf[Style]
  private[dottytags] def stick_unescaped(s: Expr[String])(using Quotes): Expr[Unescaped] = 
    import quotes.reflect._
    Apply(Ref(Symbol.requiredMethod(s"dottytags.Core._sticky._unescaped")), List(Term.of(s))).asExprOf[Unescaped]
  private def str(str: String)(using Quotes): Expr[String] = Expr(str)

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
    pprint(Term.of(s))
    s

  inline def tag(inline name: String): Unescaped = ${ nullaryTagMacro('name) }
  private def nullaryTagMacro(name: Expr[String])(using Quotes): Expr[Unescaped] = name.unlift match 
    case Some(name: String) => if isValidTagName(name) then stick_unescaped(str(s"<$name/>")) else 
              report.error(s"Not a valid XML tag name: $name");  stick_unescaped(str(""))
    case _ => report.error("Tag name must be a literal string"); stick_unescaped(str(""))

  type Child = Attr | Style | Unescaped | String
  inline def tag(inline name: String)(inline args: Child*): Unescaped = ${ tagMacro('name)('args) }
  private def tagMacro(name: Expr[String])(args: Expr[Seq[Child]])(using Quotes): Expr[Unescaped] = 
    import quotes.reflect._
    name.unlift match 
      case Some(name: String) => if !isValidTagName(name) then 
        report.error(s"Not a valid XML tag name: $name");  stick_unescaped(str("")) else { 
          args match
            case BetterVarargs(args) =>
              val attrs  = args.filter(_.isExprOf[Attr])
              val styles = args.filter(_.isExprOf[Style])
              val strs   = args.filter(s => s.isExprOf[String] || s.isExprOf[Unescaped])
              def graft(part: Expr[String], list: List[Expr[String]]): List[Expr[String]] = part match
                  case Const(s: String) => list match {
                    case Const(s0: String) :: rest => str(s0 + s) :: rest
                    case head :: rest => str(s) :: (head :: rest)
                    case List() => List(str(s))
                  }
                  case dyn => dyn :: list
              var list   = List(str(s"<$name"))
              if attrs.nonEmpty then list = graft(str(" "), list)
              attrs.foreach (attr => attr match
                case '{ _sticky._attr(_sticky._splice(${BetterVarargs(parts)}: _*)) } => parts.foreach(part => list = graft(part, list))
                case '{ _sticky._attr(${Const(s: String)}) } => list = graft(str(s), list)
                case _ => report.error("Invalid attr")
              )
              if attrs.nonEmpty then 
                list = list.head match {
                  case Const(s: String) => str(s.replaceAll("""(?m)\s+$""", "").nn) :: list.tail
                }
              if styles.nonEmpty then list = graft(str(" style=\""), list)
              styles.foreach (style => style match
                case '{ _sticky._style(_sticky._splice(${BetterVarargs(parts)}: _*)) } => parts.foreach(part => list = graft(part, list))
                case '{ _sticky._style(${Const(s: String)}) } => list = graft(str(s), list)
                case _ => report.error("Invalid style")
              )
              if styles.nonEmpty then 
                list = list.head match {
                  case Const(s: String) => str(s.replaceAll("""(?m)\s+$""", "").nn) :: list.tail
                }
                list = graft(str("\""), list)
              if strs.isEmpty    then list = graft(str("/>"), list)
              else
                list = graft(str(">"), list)
                strs.foreach (s => s match
                  case '{ _sticky._splice(${BetterVarargs(parts)}: _*) } => parts.foreach(part => list = graft('{escape($part)}, list))
                  case '{ _sticky._unescaped(_sticky._splice(${BetterVarargs(parts)}: _*)) } => parts.foreach(part => list = graft(part, list))
                  case Const(s: String) => list = graft(str(escape(s)), list)
                  case '{ _sticky._unescaped(${Const(s: String)}) } => list = graft(str(s), list)
                  case s: Expr[String] => list = '{escape($s)} :: list
                  case '{ _sticky._unescaped($s) } => list = s :: list
                  case _ => report.error("Invalid str")
                )
                list = graft(str(s"</$name>"), list)
              if (list.size == 1) then stick_unescaped(list.head)
              else stick_unescaped(stick_splice(list.reverse: _*))
            case _ => report.error("Varargs was somehow not varargs"); stick_unescaped(str(""))
        }
      case _ => report.error("Tag name must be a literal string"); stick_unescaped(str(""))
                
  inline def raw(inline raw: String): Unescaped = ${ rawMacro('raw) }
  private def rawMacro(raw: Expr[String])(using Quotes): Expr[Unescaped] = stick_unescaped(raw)

  inline def attr(inline name: String): AttrName = ${ attrNameMacro('name) }
  private def attrNameMacro(name: Expr[String])(using Quotes): Expr[AttrName] = name.unlift match 
    case Some(name: String) => if isValidAttrName(name) then stick_attrName(str(name)) else 
              report.error(s"Not a valid XML attribute name: $name");   stick_attrName(str(""))
    case _ => report.error("Attribute name must be a literal string."); stick_attrName(str(""))

  inline def css(inline name: String): StyleName = ${ styleNameMacro('name) }
  private def styleNameMacro(name: Expr[String])(using Quotes): Expr[StyleName] = name.unlift match 
    case Some(name: String) => if isValidStyleName(name) then stick_styleName(str(name)) else 
              report.error(s"Not a valid CSS style name: $name");   stick_styleName(str(""))
    case _ => report.error("Style name must be a literal string."); stick_styleName(str(""))
  
  inline def cssPx(inline name: String): StyleName = ${ styleNamePxMacro('name) }
  private def styleNamePxMacro(name: Expr[String])(using Quotes): Expr[StyleName] = name.unlift match 
    case Some(name: String) => if isValidStyleName(name) then stick_styleNamePx(str(name)) else 
              report.error(s"Not a valid CSS style name: $name");   stick_styleNamePx(str(""))
    case _ => report.error("Style name must be a literal string."); stick_styleNamePx(str(""))
  
  extension (inline name: AttrName) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ attrMacro('name, 'value) }
  private def attrMacro(name: Expr[AttrName], value: Expr[String])(using Quotes): Expr[Attr] = {
    import quotes.reflect._
    def rec(tree: Term): Option[Expr[String]] = tree match {
      case Apply(Ident("_attrName"), List(e)) => Some(e.asExprOf[String])
      case Block(List(), e) => rec(e)
      case Typed(e, _) => rec(e)
      case Inlined(_, List(), e) => rec(e)
      case _  => None
    }
    rec(Term.of(name)).flatMap(_.unlift) match
      case Some(name: String) => value.unlift match 
        case Some(value: String) => stick_attr(str(name + "=\"" + escape(value) + "\" "))
        case _ => stick_attr(stick_splice(str(name + "=\""), '{escape(${value})}, str("\" ")))
      case _ => report.error("Attribute name must be static."); stick_attr(str(""))
  }
  
  extension (inline name: StyleName) @targetName("setStyle") inline def :=(inline value: String): Style = ${ styleMacro('name, 'value) }
  private def styleMacro(name: Expr[StyleName], value: Expr[String])(using Quotes): Expr[Style] = {
    import quotes.reflect._
    def rec(tree: Term): Option[Expr[String]] = tree match {
      case Apply(Ident("_styleName"), List(e)) => Some(e.asExprOf[String])
      case Block(List(), e) => rec(e)
      case Typed(e, _) => rec(e)
      case Inlined(_, List(), e) => rec(e)
      case _  => None
    }
    rec(Term.of(name)).flatMap(_.unlift) match
      case Some(name: String) => value.unlift match 
        case Some(value: String) => stick_style(str(name + ": " + escape(value) + "; "))
        case _ => stick_style(stick_splice(str(name + ": "), '{escape(${value})}, str("; ")))
      case _ => stylePxMacro(name, value)
  }
  private def stylePxMacro(name: Expr[StyleName], value: Expr[String])(using Quotes): Expr[Style] = {
    import quotes.reflect._
    def rec(tree: Term): Option[Expr[String]] = tree match {
      case Apply(Ident("_styleNamePx"), List(e)) => Some(e.asExprOf[String])
      case Block(List(), e) => rec(e)
      case Typed(e, _) => rec(e)
      case Inlined(_, List(), e) => rec(e)
      case _  => None
    }
    rec(Term.of(name)).flatMap(_.unlift) match
      case Some(name: String) => value.unlift match 
        case Some(value: String) => stick_style(str(name + ": " + escape(value) + "px; "))
        case _ => stick_style(stick_splice(str(name + ": "), '{escape(${value})}, str("px; ")))
      case _ => report.error("Style name must be static."); stick_style(str(""))
  }

  extension (inline tag: Unescaped) inline def render(): String = ${ renderMacro('tag) }
  private def renderMacro(tag: Expr[Unescaped])(using Quotes): Expr[String] = 
    import quotes.reflect._
    tag match {
      case '{ _sticky._unescaped(_sticky._splice(${BetterVarargs(parts)}: _*)) } => {
        ValDef.let(Symbol.spliceOwner, "sb", Term.of('{ new StringBuilder() }))((sb: Ident) => Block(
          parts.map(s => Select.overloaded(sb, "append", List(TypeRepr.of[String]), List(Term.of(s)))).toList,
          Apply(Select.unique(sb, "toString"), List())
        )).asExprOf[String]
      }
      case '{ _sticky._unescaped(${Const(s: String)}) } => str(s)
      case _ => report.error("Invalid tag"); Expr("")
    }*/

}