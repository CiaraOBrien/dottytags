import minitest.*
import dottytags.*
import dottytags.utils.implicits.given
import scala.language.implicitConversions
import dottytags.predefs.tags.*
import dottytags.predefs.attrs.*
import dottytags.predefs.styles.*

/*
 * Most of these were adapted from Scalatags' test suite so as to correctly test for compatibility.
 * (see LICENSE for copyright notice)
 */

object TextTests extends SimpleTestSuite {

    test("Hello World") {
      assertXMLEquiv(div("omg"), "<div>omg</div>")
    }

    /**
     * Tests the usage of the pre-defined tags, as well as creating
     * the tags on the fly from Strings
     */
    test("Tag creation") {
      assertXMLEquiv(a.render, "<a></a>")
      assertXMLEquiv(html.render, "<html></html>")
      assertXMLEquiv(tag("this_is_an_unusual_tag").render, "<this_is_an_unusual_tag></this_is_an_unusual_tag>")
      assertXMLEquiv(tag("this-is-a-string-with-dashes", sc = true).render, "<this-is-a-string-with-dashes />")
    }

    test("CSS Helpers"){
      import dottytags.utils.cssUnits.*
      assert(10.px == "10px")
      assert(10.0.px == "10.0px" || 10.0.px == "10px")
      assert(10.em == "10em")
      assert(10.pt == "10pt")
    }

    test("Sequences of Element Types can be Frags") {
      val frag1: Frag = Seq(
        h1("titless"),
        div("lol")
      )
      val frag2: Frag = Seq("i", "am", "cow")
      val frag3: Frag = Seq(frag1, frag2)
      val wrapped = div(frag1, frag2, frag3).toString
      assert(wrapped == "<div><h1>titless</h1><div>lol</div>iamcow<h1>titless</h1><div>lol</div>iamcow</div>")
    }

    test("Namespacing") {
      val sample = tag("abc:def")(attr("hello:world") := "moo")
      assert(sample.toString == """<abc:def hello:world="moo"></abc:def>""")
    }

}