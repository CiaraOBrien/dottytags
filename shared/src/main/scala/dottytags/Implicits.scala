package dottytags

import scala.language.implicitConversions
import scala.quoted._

object implicits {

  given elemToElem: Conversion[Element, Element] = (e: Element) => e
  //given implicitFrag: Conversion[Seq[Element], Frag] = frag(_)
  given seqToFrag[A](using c: Conversion[A, Element]): Conversion[Seq[A], Frag] = (seq: Seq[A]) => bind(seq.map(c))
  //given numericToString[A](using n: Numeric[A]): Conversion[A, String] = _.toString

  trait InlineConversion[-A, +B] extends Conversion[A, B] {
    def apply(a: A): B = throw new Error("""Tried to call a macro conversion
      at runtime! This Conversion value is invalid at runtime, it must be applied
      and inlined by the compiler only, you shouldn't summon it, sorry""")
  }

}
