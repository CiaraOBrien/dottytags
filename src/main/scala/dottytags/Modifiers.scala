package dottytags

import scala.annotation.targetName
import scala.quoted.*

// === Attributes === //

/**
 * The seed of an attribute, which can be given a value with one of the [[:=]] operators,
 * or [[empty]] for a flag value. Can be [[raw]], in which case it doesn't check the validity of the name as an
 * XML attribute name (compliance with the regex `^[a-zA-Z_:][-a-zA-Z0-9_:.]*$`).
 */
final class AttrClass private (val name: String, val raw: Boolean):
  override def toString: String = name
/** [[AttrClass]] companion, private to hide [[AttrClass.apply]] */
private[dottytags] object AttrClass:
  def apply(name: String, raw: Boolean): AttrClass = new AttrClass(name, raw)

/**
 * Constructor macro for an [[AttrClass]], which enables you to specify a name, and choose whether that name should be
 * validated as an XML attribute name (based on the regex `^[a-zA-Z_:][-a-zA-Z0-9_:.]*$`), or taken as raw input.
 */
inline def attr(inline name: String, inline raw: Boolean = false): AttrClass = ${ attrClassMacro('name, 'raw) }
private def attrClassMacro(nameExpr: Expr[String], rawExpr: Expr[Boolean])(using Quotes): Expr[AttrClass] =
  Phaser.require(rawExpr, "raw attribute flag") match
    case true  => Expr(AttrClass(Phaser.require(nameExpr, "attribute name"), true))
    case false => Expr(AttrClass(validateAttrName(nameExpr),                 false))

/**
 * Converts an [[AttrClass]] into a boolean flag [[Attr]], whose value is equal to its name.
 */
extension (inline attrClass: AttrClass) inline def empty: Attr = attrClass := attrClass.name

/**
 * An XML attribute, such as `class="cls"`, where `class` is the name/[[AttrClass]] and `cls`` is the value.
 * Can be added to a [[Tag]] as part of tree construction.
 * @param name The left-hand side of the equals sign. Usually follows a particular regex, but some [[AttrClass]]es are declared raw.
 * @param value The right-hand side of the equals sign, within the quotation marks.
 */
final class Attr private (val name: String, val value: String):
  override def toString: String = name + "=\"" + value + "\""
/** [[Attr]] companion, private to hide [[Attr.apply]] */
private[dottytags] object Attr:
  def apply (name: String, value: String): Attr = new Attr(name, value)

/**
 * Constructs an XML attribute from an [[AttrClass]] and an [[Int]].
 */
extension (inline attrClass: AttrClass) @targetName("setAttrInt") inline def :=(inline value: Int): Attr = ${ setAttrIntMacro('attrClass, 'value) }
private def setAttrIntMacro(attrClass: Expr[AttrClass], value: Expr[Int])(using Quotes): Expr[Attr] =
  setAttrNoEscMacro(attrClass, PhunctionToString(Phased(value).maybeEval).expr)

/**
 * Constructs an XML attribute from an [[AttrClass]] and a [[String]], which will be HTML-escaped. The [[AttrClass]]
 * must be static (known at compile-time), the [[String]] may be static or dynamic.
 */
extension (inline attrClass: AttrClass) @targetName("setAttr") inline def :=(inline value: String): Attr = ${ setAttrMacro('attrClass, 'value) }
private def setAttrMacro(attrClassExpr: Expr[AttrClass], valueExpr: Expr[String])(using Quotes): Expr[Attr] =
  val attrClass = Phaser.require(attrClassExpr, "attribute definition")
  '{ Attr(${Expr(attrClass.name)}, ${PhunctionEscapeStr(Phased(valueExpr).maybeEval).expr}) }

/**
 * Constructs an XML attribute from an [[AttrClass]] and a [[String]], which will not be HTML-escaped. The [[AttrClass]]
 * must be static (known at compile-time), the [[String]] may be static or dynamic.
 */
private def setAttrNoEscMacro(attrClassExpr: Expr[AttrClass], valueExpr: Expr[String])(using Quotes): Expr[Attr] =
  val attrClass = Phaser.require(attrClassExpr, "attribute definition")
  '{ Attr(${Expr(attrClass.name)}, ${Phased(valueExpr).maybeEval.expr}) }

// === Styles === //

/**
 * The seed of an inline CSS style tag, which can be given a value with one of the [[:=]] operators.
 * @param name must be a valid CSS style name (approximately kebab case, `^-?[_a-zA-Z]+[_a-zA-Z0-9-]*$`)
 * @param defaultUnits if not empty, provides a unit suffix to any double or int values assigned to this [[StyleClass]].
 *                     Bypass this by passing the values as strings, or even by assigning your own units.
 */
final class StyleClass private (val name: String, val defaultUnits: String):
  override def toString: String = name
/** [[StyleClass]] companion, private to hide [[StyleClass.apply]] */
private[dottytags] object StyleClass:
  def apply (name: String, defaultUnits: String): StyleClass = new StyleClass(name, defaultUnits)

/**
 * Constructor macro for a [[StyleClass]], which enables you to specify a name, which must be validated as a CSS style name
 * (kebab-case), and which also allows you to specify a default unit suffix for any raw integer or double values that are
 * assigned to this [[StyleClass]] via [[:=]].
 */
inline def css(inline name: String, inline defaultUnits: String = ""): StyleClass = ${ styleClassMacro('name, 'defaultUnits) }
private def styleClassMacro(nameExpr: Expr[String], defaultUnitsExpr: Expr[String])(using Quotes): Expr[StyleClass] =
  Expr(StyleClass(validateStyleName(nameExpr), Phaser.require(defaultUnitsExpr, "default units")))

/**
 * An inline CSS style tag, such as `float: left;`, which will be placed within an HTML inline `style` attribute
 * if added to a [[Tag]] as part of tree construction.
 * @param name The left-hand side of the colon. Must fulfill the regex `^-?[_a-zA-Z]+[_a-zA-Z0-9-]*$`.
 * @param value The right-hand side of the colon, which may have a unit suffix.
 */
final class Style private (val name: String, val value: String):
  override def toString: String = name + ": " + value + ";"
/** [[Style]] companion, private to hide [[Style.apply]] */
private[dottytags] object Style:
  def apply (name: String, value: String): Style = new Style(name, value)

/**
 * Constructs an inline CSS style tag from a [[StyleClass]] and a [[String]], which will be HTML-escaped. The [[StyleClass]]
 * must be static (known at compile-time), the [[String]] may be static or dynamic.
 */
extension (inline styleClass: StyleClass) @targetName("setStyle") inline def :=(inline value: String): Style = ${ setStyleMacro('styleClass, 'value) }
private def setStyleMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[String])(using Quotes): Expr[Style] =
  val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
  '{ Style(${Expr(styleClass.name)}, ${PhunctionEscapeStr(Phased(valueExpr).maybeEval).expr}) }

/**
 * Constructs an inline CSS style tag from a [[StyleClass]] and a [[Double]], which will be given a default unit suffix if
 * the [[StyleClass]] in question has one declared. The [[StyleClass]] must be static (known at compile-time),
 * the [[Double]] may be static or dynamic.
 */
extension (inline styleClass: StyleClass) @targetName("setStyleUnitsDouble") inline def :=(inline value: Double): Style =
  ${ setStyleUnitsMacro('styleClass, 'value) }
private def setStyleUnitsMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[Double])(using Quotes): Expr[Style] =
  val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
  setStyleMacro(styleClassExpr, Splice.phaseSplice(PhunctionToString(Phased(valueExpr).maybeEval), Static(styleClass.defaultUnits)).expr)

/**
 * Constructs an inline CSS style tag from a [[StyleClass]] and a [[Int]], which will be given a default unit suffix if
 * the [[StyleClass]] in question has one declared. The [[StyleClass]] must be static (known at compile-time),
 * the [[Int]] may be static or dynamic.
 */
extension (inline styleClass: StyleClass) @targetName("setStyleUnitsInt") inline def :=(inline value: Int): Style =
  ${ setStyleUnitsIntMacro('styleClass, 'value) }
private def setStyleUnitsIntMacro(styleClassExpr: Expr[StyleClass], valueExpr: Expr[Int])(using Quotes): Expr[Style] =
  val styleClass: StyleClass = Phaser.require(styleClassExpr, "style definition")
  setStyleMacro(styleClassExpr, Splice.phaseSplice(PhunctionToString(Phased(valueExpr).maybeEval), Static(styleClass.defaultUnits)).expr)