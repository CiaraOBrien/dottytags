package dottytags

import scala.annotation.targetName
import scala.collection.immutable.TreeSeqMap
import scala.quoted.*
import dottytags.given

/**
 * The seed of an HTML/XML/whatever tag, which can be given child contents and attributes by [[apply]]ing it to varargs
 * of type [[Entity]]. Can be self-closing, in which case it will close like `<br />` instead of `<br></br>` if it has
 * no child [[Content]]s ([[Modifier]]s are still fair game).
 */
final class TagClass private[dottytags] (val name: String, val selfClosing: Boolean):
  override def toString: String = if selfClosing then s"<$name />" else s"<$name></$name>"
/** [[TagClass]] companion, private to hide [[TagClass.apply]] */
private[dottytags] object TagClass:
  def apply (name: String, sc: Boolean): TagClass = new TagClass(name, sc)

/**
 * Constructor macro for a [[TagClass]], which enables you to specify a name (must match `^[a-z][:\w0-9-]*$`),
 * and whether or not the tag should be self-closing like `<br />`.
 */
inline def tag(inline name: String, inline selfClosing: Boolean = false): TagClass = ${ tagClassMacro('name, 'selfClosing) }
private def tagClassMacro(nameExpr: Expr[String], selfClosingExpr: Expr[Boolean])(using Quotes): Expr[TagClass] =
  Expr(TagClass(validateTagName(nameExpr), Phaser.require(selfClosingExpr, "self-closing flag")))

/**
 * An HTML/XML/SVG/whatever element, such as `<a href="..." style="color: red;">Link<sub>Here</sub></a>`,
 * where `a` is the name/[[TagClass]], `href="..."` is an [[Attr]], `color: red;` is an inline CSS [[Style]] (hence its
 * presence in the `style` attribute), and `Link` and `<sub>Here</sub>` are child [[Content]]s of the [[Tag]].
 * @param str the spliced string expression (see [[Splice]]) that holds the textual representation of this [[Tag]] in the output.
 */
final class Tag private[dottytags] (val str: String):
  override def toString: String = str
/** [[Tag]] companion, private to hide [[Tag.apply]] */
private[dottytags] object Tag:
  def apply (s: String): Tag = new Tag(s)

/**
 * The most important macro in this library, constructs a [[Tag]] from a [[TagClass]] and some varargs of [[Entity]] values,
 * including [[Attr]]s, [[Style]]s, [[Frag]]s, [[Raw]]s, other [[Tag]]s, and normal [[String]]s (which will be ampersand-escaped
 * on their way in). Order is respected fully for [[Content]]s, but [[Modifier]]s have some special behaviour that can cause
 * them to be applied out-of-order, such as [[Attr]]s of the same name combining into the first [[Attr]]'s value quotes.
 */
extension (inline cls: TagClass) @targetName("tagApplyBody") inline def apply(inline entities: Entity*): Tag = ${ tagMacro('cls)('entities) }
private def tagMacro(clsExpr: Expr[TagClass])(varargs: Expr[Seq[Entity]])(using Quotes): Expr[Tag] = {
  val cls = Phaser.require(clsExpr, "tag class")
  // The canonical ordered list of modifiers, including the `style` attribute if there are any inline styles.
  // TreeSeqMap is used because it's a Map that preserves insertion order, which is how attribute concatenation is supposed to work.
  // The values are Phasers because we need to have proper splicing and desplicing.
  var canonMods = TreeSeqMap[String, Phaser[String]]()
  // Go over the varargs jut for Modifiers (Attrs and Styles), we will go over again for Contents later.
  Varargs.unapply(varargs).foreach { _.foreach {
    case '{ Attr($nameExpr : String, $valueExpr: String) } => val name = Phaser.require(nameExpr, "attribute name")
      if name == "style" && canonMods.contains(name) then // An explicit "style" attribute overwrites any previous "style" attribute contents entirely.
        canonMods = canonMods.updated(name, Splice.phaseSplice(Static("=\""), Phased(valueExpr).maybeEval)) // The new splice starts with =".
      else if canonMods.contains(name) then // We have a matching attribute already, concatenate the two attrs' values
        canonMods = canonMods.updated(name, Splice.phaseSplice(canonMods.get(name).get, Static(" "), Phased(valueExpr).maybeEval) )
      else // The new attribute value splice starts with =", the closing " will be added to each attribute value at the end.
        canonMods = canonMods.updated(name, Splice.phaseSplice(Static("=\""), Phased(valueExpr).maybeEval))
    case '{ Style($nameExpr: String, $valueExpr: String) } => val name = Phaser.require(nameExpr, "style name")
      if canonMods.contains("style") then // If there is already a "style" attribute (either some previous inline styles, or an explicit "style" attr), update it
        canonMods = canonMods.updated("style", Splice.phaseSplice(canonMods.get("style").get, Static(" "), Static(name),
            Static(": "), Phased(valueExpr).maybeEval, Static(";")) )
      else // If there is no "style" attribute, make one.
        canonMods = canonMods.updated("style", Splice.phaseSplice(Static("=\""), Static(name),
            Static(": "), Phased(valueExpr).maybeEval, Static(";")))
    // These restrictions are necessary because fuck the very notion of parsing and editing the existing string value at runtime
    // to figure out where phased attrs and styles should go.
    case '{ $attr: Attr   } => error("Attributes cannot be fully dynamic, only their values may vary at runtime.", attr)
    case '{ $style: Style } => error("Styles cannot be fully dynamic, only their values may vary at runtime.", style)
    case _ => () // Ignore Contents, we only care about Modifiers in this loop
  }}
  // Splice together all the modifiers, including a leading space, the attr name, =", values, and a closing ".
  val modifierSplice = Splice.phaseSplice(canonMods.map((name, splice) =>
      Splice.phaseSplice(Static(" "), Static(name), splice, Static("\""))).toSeq*)
  // Splice together all the tag contents, ignoring the modifiers, escaping string spans, be they static or phased.
  val contentsSplice = Varargs.unapply(varargs).map(exprs => Splice.phaseSplice(exprs.collect {
      case '{ $str:  String } => PhunctionEscapeStr(Phased(str).maybeEval)
      case '{ Tag($str)     } => Phased(str).maybeEval
      case '{ Raw($str)     } => Phased(str).maybeEval
      case '{ Frag($str)    } => Phased(str).maybeEval
      case '{ $tag:  Tag    } => Phased('{$tag.str}).maybeEval
      case '{ $raw:  Raw    } => Phased('{$raw.str}).maybeEval
      case '{ $frag: Frag   } => Phased('{$frag.str}).maybeEval
    }*) // phaseSplice takes varargs
  ).getOrElse(Static("")) // Just have it empty if there are no varargs.
  // Construct the final Tag.apply invocation with a splice inside.
  contentsSplice match {
    case Static("") if cls.selfClosing => // If the content is empty and this is a self-closing tag, self-close! (add modifiers still though)
        '{ Tag(${ Splice.phaseSplice(Static("<"), Static(cls.name), modifierSplice, Static(" />")).expr }) }
    case contents => '{ Tag(${ Splice.phaseSplice(Static("<"), Static(cls.name), modifierSplice, Static(">"),
                                                  contents, Static("</"), Static(cls.name), Static(">")).expr }) }
  }
}