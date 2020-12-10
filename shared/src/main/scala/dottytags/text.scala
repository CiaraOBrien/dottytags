package dottytags

import scala.quoted._
import scala.annotation.tailrec

object text {

  final class AttrName private[text] (name: String) {

  }

  final class Attr private[text] (name: AttrName, value: String) {
    
  }

}