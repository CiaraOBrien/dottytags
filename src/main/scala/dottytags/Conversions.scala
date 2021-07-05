package dottytags

import scala.collection.generic.IsIterable
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.language.implicitConversions
import scala.quoted.*

object syntax {
  
  given contentToContent: Conversion[Content, Content] = (e: Content) => e

    // As far as I know this works alright, but it doesn't actually handle the potential for the expression's value
    // to be directly manipulated (cf. Phaser), it just tacks wrapper logic onto the expression automatically,
    // which is probably fine but still grinds my gears. However implementing Phaser logic in here would be a nightmare.
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

    given arrayToFrag [A](using c: Conversion[A, Content]): Conversion[Array [A], Frag] = (arr: Array [A]) => bind(arr.toIndexedSeq.map(c))
    given optionToFrag[A](using c: Conversion[A, Content]): Conversion[Option[A], Frag] = (opt: Option[A]) => bind(opt.toList.map(c))
    given seqToFrag   [A](using c: Conversion[A, Content]): Conversion[Seq   [A], Frag] = (seq: Seq   [A]) => bind(seq.map(c))

}
