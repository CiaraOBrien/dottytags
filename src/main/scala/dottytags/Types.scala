package dottytags

import scala.quoted.*

type Entity = Modifier | Content
type Modifier = Attr | Style
type Content = Tag | Frag | Raw | String

final class Frag private[dottytags] (val str: String) {
    override def toString: String = str
}
private[dottytags] object Frag {
    def apply (s: String): Frag = new Frag(s)
}
given FragFromExpr: FromExpr[Frag] with {
    def unapply(x: Expr[Frag])(using Quotes): Option[Frag] = x match
        case '{     Frag(${Expr(str: String)}) } => Some(Frag(str))
        case '{ new Frag(${Expr(str: String)}) } => Some(Frag(str))
        case _ => None
}
given FragToExpr: ToExpr[Frag] with {
    def apply(x: Frag)(using Quotes): Expr[Frag] = '{ Frag(${Expr(x.str)}) }
}

inline def frag(inline parts: Content*): Frag = ${ fragMacro('parts) }
private def fragMacro(partsExpr: Expr[Seq[Content]])(using Quotes): Expr[Frag] = partsExpr match {
    case Varargs(exprs) => '{ Frag(${ Splice.phaseSplice(exprs.map {
        case '{ $str:  String } => PhunctionEscapeStr(Phaser.NextPhase(str).pull)
        case '{ Tag($str)     } => Phaser.NextPhase(str).pull
        case '{ Raw($str)     } => Phaser.NextPhase(str).pull
        case '{ Frag($str)    } => Phaser.NextPhase(str).pull
        case '{ $tag:  Tag    } => Phaser.NextPhase('{$tag.str}).pull
        case '{ $raw:  Raw    } => Phaser.NextPhase('{$raw.str}).pull
        case '{ $frag: Frag   } => Phaser.NextPhase('{$frag.str}).pull
        case e => error(s"Unrecognised frag component: ${e.show}", e)
    }*).expr }) }
    case _ => error(s"Input to \"frag\" must be varargs, use \"bind\" instead.", partsExpr)
}

inline def bind(inline seq: Seq[Content]): Frag = ${ bindMacro('seq) }
private def bindMacro(seqExpr: Expr[Seq[Content]])(using Quotes): Expr[Frag] = '{ Frag(spliceContent($seqExpr)) }
private def spliceContent(seq: Seq[Content]): String = {
    val sb = new StringBuilder()
    var i = 0
    val len = seq.size
    while i < len do {
        sb.append(seq(i).toString)
        i = i + 1
    }
    sb.toString
}

final class Raw private[dottytags] (val str: String) {
    override def toString: String = str
}
private[dottytags] object Raw {
    def apply (s: String): Raw = new Raw(s)
}
given RawFromExpr: FromExpr[Raw] with {
    def unapply(x: Expr[Raw])(using Quotes): Option[Raw] = x match
        case '{     Raw(${Expr(str: String)}) } => Some(Raw(str))
        case '{ new Raw(${Expr(str: String)}) } => Some(Raw(str))
        case _ => None
}
given RawToExpr: ToExpr[Raw] with {
    def apply(x: Raw)(using Quotes): Expr[Raw] = '{ Raw(${Expr(x.str)}) }
}

inline def raw(inline str: String): Raw = ${ rawMacro('str) }
private def rawMacro(expr: Expr[String])(using Quotes): Expr[Raw] = '{ Raw($expr) }
inline def rawSplice(inline parts: String*): Raw = ${ rawSpliceMacro('parts) }
private def rawSpliceMacro(partsExpr: Expr[Seq[String]])(using Quotes): Expr[Raw] = partsExpr match {
    case Varargs(exprs) => '{ Raw(${ Splice.phaseSplice(exprs.map(expr => Phaser.NextPhase(expr).pull)*).expr }) }
    case _ => error("\"parts\" must be varargs.", partsExpr)
}