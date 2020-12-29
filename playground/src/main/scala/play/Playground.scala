package play

import dottytags._
import dottytags.implicits.given
import scala.language.implicitConversions
import dottytags.tags._
import dottytags.svg._
import dottytags.attrs._
import dottytags.styles._

object Playground:
  def main(args: Array[String]): Unit =
    Phased.phasedTest("test")
    val test = "test"
    Phased.phasedTest(test)