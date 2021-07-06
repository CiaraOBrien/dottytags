package dottytags

import scala.quoted.*

/*
 * Documentation marked "MDN" is thanks to Mozilla Contributors
 * at https://developer.mozilla.org/en-US/docs/Web/API and available
 * under the Creative Commons Attribution-ShareAlike v2.5 or later.
 * http://creativecommons.org/licenses/by-sa/2.5/
 *
 * Everything else is under the MIT License, see here:
 * https://github.com/CiaraOBrien/dottytags/blob/main/LICENSE
 *
 * This whole file is, of course, adapted from Scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/DataTypes.scala
 */

object units {
  
  /**
   * Macro to simplify all of the unit declarations, they all just call it with a different string parameter.
   */
  private inline def unit[A : Numeric](inline n: A, inline unit: String) = ${ unitMacro('n, 'unit) }
  private def unitMacro[A : Type](value: Expr[A], unit: Expr[String])(using Quotes): Expr[String] = value match {
    case '{ $n: Double } => Splice.phaseSplice(PhunctionToString(Phased(n).maybeEval), Phased(unit).maybeEval).expr
    case '{ $n: Int    } => Splice.phaseSplice(PhunctionToString(Phased(n).maybeEval), Phased(unit).maybeEval).expr
    case _ => '{$value.toString.+($unit)}
  }


  extension [A : Numeric](inline n: A) {

    /**
      * Relative to the viewing device. For screen display, typically one device
      * pixel (dot) of the display.
      *
      * For printers and very high resolution screens one CSS pixel implies
      * multiple device pixels, so that the number of pixel per inch stays around
      * 96.
      *
      * MDN
      */
    inline def px = unit(n, "px")


    /**
      * One point which is 1/72 of an inch.
      *
      * MDN
      */
    inline def pt = unit(n, "pt")

    /**
      * One millimeter.
      *
      * MDN
      */
    inline def mm = unit(n, "mm")

    /**
      * One centimeter 10 millimeters.
      *
      * MDN
      */
    inline def cm = unit(n, "cm")

    /**
      * One inch 2.54 centimeters.
      *
      * MDN
      */
    inline def in = unit(n, "in")

    /**
      * One pica which is 12 points.
      *
      * MDN
      */
    inline def pc = unit(n, "pc")
    /**
      * This unit represents the calculated font-size of the element. If used on
      * the font-size property itself, it represents the inherited font-size
      * of the element.
      *
      * MDN
      */
    inline def em = unit(n, "em")

    /**
      * This unit represents the width, or more precisely the advance measure, of
      * the glyph '0' zero, the Unicode character U+0030 in the element's font.
      *
      * MDN
      */
    inline def ch = unit(n, "ch")

    /**
      * This unit represents the x-height of the element's font. On fonts with the
      * 'x' letter, this is generally the height of lowercase letters in the font;
      * 1ex ≈ 0.5em in many fonts.
      *
      * MDN
      */
    inline def ex = unit(n, "ex")

    /**
      * This unit represents the font-size of the root element e.g. the font-size
      * of the html element. When used on the font-size on this root element,
      * it represents its initial value.
      *
      * MDN
      */
    inline def rem = unit(n, "rem")

    /**
      * An angle in degrees. One full circle is 360deg. E.g. 0deg, 90deg, 360deg.
      */
    inline def deg = unit(n, "deg")

    /**
      * An angle in gradians. One full circle is 400grad. E.g. 0grad, 100grad,
      * 400grad.
      *
      * MDN
      */
    inline def grad = unit(n, "grad")

    /**
      * An angle in radians.  One full circle is 2π radians which approximates
      * to 6.2832rad. 1rad is 180/π degrees. E.g. 0rad, 1.0708rad, 6.2832rad.
      *
      * MDN
      */
    inline def rad = unit(n, "rad")

    /**
      * The number of turns the angle is. One full circle is 1turn. E.g. 0turn,
      * 0.25turn, 1turn.
      *
      * MDN
      */
    inline def turn = unit(n, "turn")

    /**
      * A percentage value
      */
    inline def pct = unit(n, "%")
  }

}
