package dottytags

import scala.quoted.*

// So many FromExprs and ToExprs!

private[dottytags] given AttrClassFromExpr: FromExpr[AttrClass] with
  def unapply(x: Expr[AttrClass])(using Quotes): Option[AttrClass] = x match
    case '{     AttrClass(${Expr(name: String)}, ${Expr(raw: Boolean)})} => Some(AttrClass(name, raw))
    case _ => None
private[dottytags] given AttrClassToExpr: ToExpr[AttrClass] with
  def apply(x: AttrClass)(using Quotes): Expr[AttrClass] = '{ AttrClass(${Expr(x.name)}, ${Expr(x.raw)}) }
 

private[dottytags] given AttrFromExpr: FromExpr[Attr] with
  def unapply(x: Expr[Attr])(using Quotes): Option[Attr] = x match
    case '{ Attr(${Expr(name: String)}, ${Expr(value: String)}) } => Some(Attr(name, value))
    case _ => None
private[dottytags] given AttrToExpr: ToExpr[Attr] with
  def apply(x: Attr)(using Quotes): Expr[Attr] = '{ Attr(${Expr(x.name)}, ${Expr(x.value)}) }
  

private[dottytags] given StyleClassFromExpr: FromExpr[StyleClass] with
  def unapply(x: Expr[StyleClass])(using Quotes): Option[StyleClass] = x match
    case '{ StyleClass(${Expr(name: String)}, ${Expr(defaultUnits: String)})} => Some(StyleClass(name, defaultUnits))
    case _ => None
private[dottytags] given StyleClassToExpr: ToExpr[StyleClass] with
  def apply(x: StyleClass)(using Quotes): Expr[StyleClass] = '{ StyleClass(${Expr(x.name)}, ${Expr(x.defaultUnits)}) }


private[dottytags] given StyleFromExpr: FromExpr[Style] with
  def unapply(x: Expr[Style])(using Quotes): Option[Style] = x match
    case '{ Style(${Expr(name: String)}, ${Expr(value: String)}) } => Some(Style(name, value))
    case _ => None
private[dottytags] given StyleToExpr: ToExpr[Style] with
  def apply(x: Style)(using Quotes): Expr[Style] = '{ Style(${Expr(x.name)}, ${Expr(x.value)}) }
  
  
private[dottytags] given TagClassFromExpr: FromExpr[TagClass] with
  def unapply(x: Expr[TagClass])(using Quotes): Option[TagClass] = x match
    case '{     TagClass(${Expr(name: String)}, ${Expr(sc: Boolean)}) } => Some(TagClass(name, sc))
    case '{ new TagClass(${Expr(name: String)}, ${Expr(sc: Boolean)}) } => Some(TagClass(name, sc))
    case _ => None
private[dottytags] given TagClassToExpr: ToExpr[TagClass] with
  def apply(x: TagClass)(using Quotes): Expr[TagClass] = '{ TagClass(${Expr(x.name)}, ${Expr(x.selfClosing)}) }

  
private[dottytags] given TagFromExpr: FromExpr[Tag] with
  def unapply(x: Expr[Tag])(using Quotes): Option[Tag] = x match
    case '{     Tag(${Expr(str: String)}) } => Some(Tag(str))
    case '{ new Tag(${Expr(str: String)}) } => Some(Tag(str))
    case _ => None
private[dottytags] given TagToExpr: ToExpr[Tag] with
  def apply(x: Tag)(using Quotes): Expr[Tag] = '{ Tag(${Expr(x.str)}) }
  

private[dottytags] given FragFromExpr: FromExpr[Frag] with
  def unapply(x: Expr[Frag])(using Quotes): Option[Frag] = x match
    case '{     Frag(${Expr(str: String)}) } => Some(Frag(str))
    case '{ new Frag(${Expr(str: String)}) } => Some(Frag(str))
    case _ => None
private[dottytags] given FragToExpr: ToExpr[Frag] with
  def apply(x: Frag)(using Quotes): Expr[Frag] = '{ Frag(${Expr(x.str)}) }
  

private[dottytags] given RawFromExpr: FromExpr[Raw] with
  def unapply(x: Expr[Raw])(using Quotes): Option[Raw] = x match
    case '{     Raw(${Expr(str: String)}) } => Some(Raw(str))
    case '{ new Raw(${Expr(str: String)}) } => Some(Raw(str))
    case _ => None
private[dottytags] given RawToExpr: ToExpr[Raw] with
  def apply(x: Raw)(using Quotes): Expr[Raw] = '{ Raw(${Expr(x.str)}) }
