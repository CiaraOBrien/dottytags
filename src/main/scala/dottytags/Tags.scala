package dottytags

import scala.annotation.targetName
import scala.collection.immutable.TreeSeqMap
import scala.quoted.*

extension (inline cls: TagClass) @targetName("tagApplyBody") inline def apply(inline entities: Entity*): Tag = ${ tagMacro('cls)('entities) }
private def tagMacro(clsExpr: Expr[TagClass])(varargs: Expr[Seq[Entity]])(using Quotes): Expr[Tag] = {
  val cls = Phaser.require(clsExpr, "tag class")
  var canonMods = TreeSeqMap[String, Phaser[String]]()
  Varargs.unapply(varargs).foreach { _.foreach {
    case '{ Attr($nameExpr : String, $valueExpr: String) } => val name = Phaser.require(nameExpr, "attribute name")
      if name == "style" && canonMods.contains(name) then
        canonMods = canonMods.updated(name, Splice.phaseSplice(Phaser.ThisPhase("=\""), Phaser.NextPhase(valueExpr).pull))
      else if canonMods.contains(name) then
        canonMods = canonMods.updated(name, Splice.phaseSplice(canonMods.get(name).get, Phaser.ThisPhase(" "), Phaser.NextPhase(valueExpr).pull) )
      else
        canonMods = canonMods.updated(name, Splice.phaseSplice(Phaser.ThisPhase("=\""), Phaser.NextPhase(valueExpr).pull))
    case '{ Style($nameExpr: String, $valueExpr: String) } => val name = Phaser.require(nameExpr, "style name")
      if canonMods.contains("style") then
        canonMods = canonMods.updated("style", Splice.phaseSplice(canonMods.get("style").get, Phaser.ThisPhase(" "), Phaser.ThisPhase(name),
            Phaser.ThisPhase(": "), Phaser.NextPhase(valueExpr).pull, Phaser.ThisPhase(";")) )
      else
        canonMods = canonMods.updated("style", Splice.phaseSplice(Phaser.ThisPhase("=\""), Phaser.ThisPhase(name),
            Phaser.ThisPhase(": "), Phaser.NextPhase(valueExpr).pull, Phaser.ThisPhase(";")))
    case '{ $attr: Attr   } => error("Attributes cannot be fully dynamic, only their values may vary at runtime.", attr)
    case '{ $style: Style } => error("Styles cannot be fully dynamic, only their values may vary at runtime.", style)
    case _ => ()
  }}
  val modifierSplice = Splice.phaseSplice(canonMods.map((name, splice) =>
      Splice.phaseSplice(Phaser.ThisPhase(" "), Phaser.ThisPhase(name), splice, Phaser.ThisPhase("\""))).toSeq*)
  val contentsSplice = Varargs.unapply(varargs).map(exprs => Splice.phaseSplice(exprs.collect {
      case '{ $str:  String } => PhunctionEscapeStr(Phaser.NextPhase(str).pull)
      case '{ Tag($str)     } => Phaser.NextPhase(str).pull
      case '{ Raw($str)     } => Phaser.NextPhase(str).pull
      case '{ Frag($str)    } => Phaser.NextPhase(str).pull
      case '{ $tag:  Tag    } => Phaser.NextPhase('{$tag.str}).pull
      case '{ $raw:  Raw    } => Phaser.NextPhase('{$raw.str}).pull
      case '{ $frag: Frag   } => Phaser.NextPhase('{$frag.str}).pull
    }*)
  ).getOrElse(Phaser.ThisPhase(""))
  contentsSplice match {
    case Phaser.ThisPhase("") if cls.selfClosing =>
        '{ Tag(${ Splice.phaseSplice(Phaser.ThisPhase("<"), Phaser.ThisPhase(cls.name), modifierSplice, Phaser.ThisPhase(" />")).expr }) }
    case cs => '{ Tag(${ Splice.phaseSplice(Phaser.ThisPhase("<"), Phaser.ThisPhase(cls.name), modifierSplice, Phaser.ThisPhase(">"),
        cs, Phaser.ThisPhase("</"), Phaser.ThisPhase(cls.name), Phaser.ThisPhase(">")).expr }) }
  }
}
final class Tag private[dottytags] (val str: String) {
  override def toString: String = str
}
private[dottytags] object Tag {
  def apply (s: String): Tag = new Tag(s)
}
given TagFromExpr: FromExpr[Tag] with {
  def unapply(x: Expr[Tag])(using Quotes): Option[Tag] = x match
    case '{     Tag(${Expr(str: String)}) } => Some(Tag(str))
    case '{ new Tag(${Expr(str: String)}) } => Some(Tag(str))
    case _ => None
}
given TagToExpr: ToExpr[Tag] with {
  def apply(x: Tag)(using Quotes): Expr[Tag] = '{ Tag(${Expr(x.str)}) }
}

final class TagClass private[dottytags] (val name: String, val selfClosing: Boolean) {
  override def toString: String = if selfClosing then s"<$name />" else s"<$name></$name>"
}
private[dottytags] object TagClass {
  def apply (name: String, sc: Boolean): TagClass = new TagClass(name, sc)
}
given TagClassFromExpr: FromExpr[TagClass] with {
  def unapply(x: Expr[TagClass])(using Quotes): Option[TagClass] = x match
      case '{     TagClass(${Expr(name: String)}, ${Expr(sc: Boolean)}) } => Some(TagClass(name, sc))
      case '{ new TagClass(${Expr(name: String)}, ${Expr(sc: Boolean)}) } => Some(TagClass(name, sc))
      case _ => None
}
given TagClassToExpr: ToExpr[TagClass] with {
  def apply(x: TagClass)(using Quotes): Expr[TagClass] = '{ TagClass(${Expr(x.name)}, ${Expr(x.selfClosing)}) }
}

inline def tag(inline name: String, inline selfClosing: Boolean = false): TagClass = ${ tagClassMacro('name, 'selfClosing) }
private def tagClassMacro(nameExpr: Expr[String], selfClosingExpr: Expr[Boolean])(using Quotes): Expr[TagClass] =
    Expr(TagClass(validateTagName(nameExpr), Phaser.require(selfClosingExpr, "self-closing flag")))