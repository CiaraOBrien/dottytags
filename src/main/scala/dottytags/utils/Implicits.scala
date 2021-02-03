package dottytags.utils

import scala.language.implicitConversions
import scala.quoted._
import scala.collection.generic.IsIterable
import collection.IterableOnce.iterableOnceExtensionMethods

import dottytags._

object syntax {

  given elemToElem: Conversion[Element, Element] = (e: Element) => e
  //given implicitFrag: Conversion[Seq[Element], Frag] = frag(_)

  trait InlineConversion[-A, +B] extends Conversion[A, B] {
    def apply(a: A): B = throw new Error("""Tried to call a macro conversion
      at runtime! This Conversion value is invalid at runtime, it must be applied
      and inlined by the compiler only, you shouldn't summon it, sorry""")
  }

  given numericToString[A : Numeric]: InlineConversion[A, String] with {
    override inline def apply(a: A): String = a.toString
  }
  given booleanToString: InlineConversion[Boolean, String] with {
    override inline def apply(b: Boolean): String = if b then "true" else "false"
  }
  given unitToString: InlineConversion[Unit, String] with {
    override inline def apply(u: Unit): String = ""
  }

  given arrayToFrag [A](using c: Conversion[A, Element]): Conversion[Array [A], Frag] = (arr: Array [A]) => bind(arr.toSeq.map(c))
  given optionToFrag[A](using c: Conversion[A, Element]): Conversion[Option[A], Frag] = (opt: Option[A]) => bind(opt.toList.map(c))
  given seqToFrag   [A](using c: Conversion[A, Element]): Conversion[Seq   [A], Frag] = (seq: Seq   [A]) => bind(seq.map(c))

}
