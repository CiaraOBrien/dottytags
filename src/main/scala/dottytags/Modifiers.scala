package dottytags

import scala.annotation.targetName
import scala.quoted.*

// === Attributes === //

final class AttrClass private (val name: String, val raw: Boolean):
    override def toString: String = name
private[dottytags] object AttrClass:
    def apply(name: String, raw: Boolean): AttrClass = new AttrClass(name, raw)
given AttrClassFromExpr: FromExpr[AttrClass] with
    def unapply(x: Expr[AttrClass])(using Quotes): Option[AttrClass] = x match
        case '{     AttrClass(${Expr(name: String)}, ${Expr(raw: Boolean)})} => Some(AttrClass(name, raw))
        case _ => None
given AttrClassToExpr: ToExpr[AttrClass] with
    def apply(x: AttrClass)(using Quotes): Expr[AttrClass] = '{ AttrClass(${Expr(x.name)}, ${Expr(x.raw)}) }

inline def attr(inline name: String, inline raw: Boolean = false): AttrClass = ${ attrClassMacro('name, 'raw) }
private def attrClassMacro(nameExpr: Expr[String], rawExpr: Expr[Boolean])(using Quotes): Expr[AttrClass] =
    Phaser.require(rawExpr, "raw attribute flag") match
        case true  => Expr(AttrClass(Phaser.require(nameExpr, "attribute name"), true))
        case false => Expr(AttrClass(validateAttrName(nameExpr),                 false))

final class Attr private (val name: String, val value: String):
    override def toString: String = name + "=\"" + value + "\""
private[dottytags] object Attr:
    def apply (name: String, value: String): Attr = new Attr(name, value)
given AttrFromExpr: FromExpr[Attr] with
    def unapply(x: Expr[Attr])(using Quotes): Option[Attr] = x match
        case '{ Attr(${Expr(name: String)}, ${Expr(value: String)}) } => Some(Attr(name, value))
        case _ => None
given AttrToExpr: ToExpr[Attr] with
    def apply(x: Attr)(using Quotes): Expr[Attr] = '{ Attr(${Expr(x.name)}, ${Expr(x.value)}) }

extension (inline attrClass: AttrClass) @targetName("setAttrInt") inline def :=(inline value: Int): Attr = ${ setAttrIntMacro('attrClass, 'value) }
private def setAttrIntMacro(attrClass: Expr[AttrClass], value: Expr[Int])(using Quotes): Expr[Attr] = setAttrNoEscMacro(attrClass, PhunctionToString(Phaser.NextPhase(value).pull).expr)
extension (inline attrClass: AttrClass) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ setAttrMacro('attrClass, 'value) }
private def setAttrMacro(attrClassExpr: Expr[AttrClass], valueExpr: Expr[String])(using Quotes): Expr[Attr] =
    val attrClass = Phaser.require(attrClassExpr, "attribute definition")
    '{ Attr(${Expr(attrClass.name)}, ${PhunctionEscapeStr(Phaser.NextPhase(valueExpr).pull).expr}) }
private def setAttrNoEscMacro(attrClassExpr: Expr[AttrClass], valueExpr: Expr[String])(using Quotes): Expr[Attr] =
    val attrClass = Phaser.require(attrClassExpr, "attribute definition")
    '{ Attr(${Expr(attrClass.name)}, ${Phaser.NextPhase(valueExpr).pull.expr}) }

// === Styles === //

final class Style private (val name: String, val value: String):
    override def toString: String = name + ": " + value + ";"
private[dottytags] object Style:
    def apply (name: String, value: String): Style = new Style(name, value)
given StyleFromExpr: FromExpr[Style] with
    def unapply(x: Expr[Style])(using Quotes): Option[Style] = x match
        case '{ Style(${Expr(name: String)}, ${Expr(value: String)}) } => Some(Style(name, value))
        case _ => None
given StyleToExpr: ToExpr[Style] with
    def apply(x: Style)(using Quotes): Expr[Style] = '{ Style(${Expr(x.name)}, ${Expr(x.value)}) }

final class StyleClass private (val name: String, val defaultUnits: String):
    override def toString: String = name
private[dottytags] object StyleClass:
    def apply (name: String, defaultUnits: String): StyleClass = new StyleClass(name, defaultUnits)
given StyleClassFromExpr: FromExpr[StyleClass] with
    def unapply(x: Expr[StyleClass])(using Quotes): Option[StyleClass] = x match
        case '{ StyleClass(${Expr(name: String)}, ${Expr(defaultUnits: String)})} => Some(StyleClass(name, defaultUnits))
        case _ => None
given StyleClassToExpr: ToExpr[StyleClass] with
    def apply(x: StyleClass)(using Quotes): Expr[StyleClass] = '{ StyleClass(${Expr(x.name)}, ${Expr(x.defaultUnits)}) }

inline def css(inline name: String, inline defaultUnits: String = ""): StyleClass = ${ styleClassMacro('name, 'defaultUnits) }
private def styleClassMacro(nameExpr: Expr[String], defaultUnitsExpr: Expr[String])(using Quotes): Expr[StyleClass] =
    Expr(StyleClass(validateStyleName(nameExpr), Phaser.require(defaultUnitsExpr, "default units")))
extension (inline styleClass: StyleClass) @targetName("setStyle") inline def :=(inline value: String): Style = ${ setStyleMacro('styleClass, 'value) }
private def setStyleMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[String])(using Quotes): Expr[Style] = {
    val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
    '{ Style(${Expr(styleClass.name)}, ${PhunctionEscapeStr(Phaser.NextPhase(valueExpr).pull).expr}) }
}

extension (inline styleClass: StyleClass) @targetName("setStyleUnitsDouble") inline def :=(inline value: Double): Style =
    ${ setStyleUnitsMacro('styleClass, 'value) }
private def setStyleUnitsMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[Double])(using Quotes): Expr[Style] = {
    val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
    setStyleMacro(styleClassExpr, Splice.phaseSplice(PhunctionToString(Phaser.NextPhase(valueExpr).pull), Phaser.ThisPhase(styleClass.defaultUnits)).expr)
}

extension (inline styleClass: StyleClass) @targetName("setStyleUnitsInt") inline def :=(inline value: Int): Style =
    ${ setStyleUnitsIntMacro('styleClass, 'value) }
private def setStyleUnitsIntMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[Int])(using Quotes): Expr[Style] = {
    val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
    setStyleMacro(styleClassExpr, Splice.phaseSplice(PhunctionToString(Phaser.NextPhase(valueExpr).pull), Phaser.ThisPhase(styleClass.defaultUnits)).expr)
}

extension (inline attrClass: AttrClass) inline def empty: Attr = attrClass := attrClass.name