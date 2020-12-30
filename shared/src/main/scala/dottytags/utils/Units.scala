package dottytags.utils

/*
 * Documentation marked "MDN" is thanks to Mozilla Contributors
 * at https://developer.mozilla.org/en-US/docs/Web/API and available
 * under the Creative Commons Attribution-ShareAlike v2.5 or later.
 * http://creativecommons.org/licenses/by-sa/2.5/
 *
 * Everything else is under the MIT License, see here:
 * https://github.com/CiaraOBrien/dottytags/blob/main/LICENSE
 * 
 * This whole file is, of course, adapted from scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/DataTypes.scala
 */

/**
  * Extends numbers to provide a bunch of useful methods, allowing you to write
  * CSS-lengths in a nice syntax without resorting to strings.
  */
object cssUnits {

  extension[A : Numeric](x: A) {

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
    inline def px = s"${x}px"

    /**
      * One point which is 1/72 of an inch.
      *
      * MDN
      */
    inline def pt = s"${x}pt"

    /**
      * One millimeter.
      *
      * MDN
      */
    inline def mm = s"${x}mm"

    /**
      * One centimeter 10 millimeters.
      *
      * MDN
      */
    inline def cm = s"${x}cm"

    /**
      * One inch 2.54 centimeters.
      *
      * MDN
      */
    inline def in = s"${x}in"

    /**
      * One pica which is 12 points.
      *
      * MDN
      */
    inline def pc = s"${x}pc"
    /**
      * This unit represents the calculated font-size of the element. If used on
      * the font-size property itself, it represents the inherited font-size
      * of the element.
      *
      * MDN
      */
    inline def em = s"${x}em"

    /**
      * This unit represents the width, or more precisely the advance measure, of
      * the glyph '0' zero, the Unicode character U+0030 in the element's font.
      *
      * MDN
      */
    inline def ch = s"${x}ch"

    /**
      * This unit represents the x-height of the element's font. On fonts with the
      * 'x' letter, this is generally the height of lowercase letters in the font;
      * 1ex ≈ 0.5em in many fonts.
      *
      * MDN
      */
    inline def ex = s"${x}ex"

    /**
      * This unit represents the font-size of the root element e.g. the font-size
      * of the html element. When used on the font-size on this root element,
      * it represents its initial value.
      *
      * MDN
      */
    inline def rem = s"${x}rem"

    /**
      * An angle in degrees. One full circle is 360deg. E.g. 0deg, 90deg, 360deg.
      */
    inline def deg = s"${x}deg"

    /**
      * An angle in gradians. One full circle is 400grad. E.g. 0grad, 100grad,
      * 400grad.
      *
      * MDN
      */
    inline def grad = s"${x}grad"

    /**
      * An angle in radians.  One full circle is 2π radians which approximates
      * to 6.2832rad. 1rad is 180/π degrees. E.g. 0rad, 1.0708rad, 6.2832rad.
      *
      * MDN
      */
    inline def rad = s"${x}rad"

    /**
      * The number of turns the angle is. One full circle is 1turn. E.g. 0turn,
      * 0.25turn, 1turn.
      *
      * MDN
      */
    inline def turn = s"${x}turn"

    /**
      * A percentage value
      */
    inline def pct = s"${x}%"

  }

}