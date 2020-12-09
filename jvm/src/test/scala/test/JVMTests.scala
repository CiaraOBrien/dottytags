package dottytags

import utest._
import dottytags.Core._

object JVMTests extends TestSuite {
  val tests = Tests {
    test("isValidTag") {
      //assert(Escape.isValidTag("abc_"))
      //assert(!Escape.isValidTag("abc."))
      //assert(!Escape.isValidTag(capitalize("abc_")))
    }
  }
}