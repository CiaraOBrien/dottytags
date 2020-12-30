import minitest._
import dottytags._
import dottytags.utils.implicits.given
import scala.language.implicitConversions
import dottytags.predefs.tags._
import dottytags.predefs.attrs._
import dottytags.predefs.styles._

import scala.language.implicitConversions

object CorrectnessTests extends SimpleTestSuite {

  test("Basic Tree Building") {
    val x = script("")
    assertXMLEquiv(
      html(
        head(x, tag("string-tag")("Hi how are you")),
        body(div(p))
      ),
      """
      |<html>
      |    <head>
      |        <script></script>
      |        <string-tag>Hi how are you</string-tag>
      |    </head>
      |    <body>
      |        <div>
      |            <p></p>
      |        </div>
      |    </body>
      |</html>
      """.stripMargin
    )
  }

  test("CSS Style Chaining") { assertXMLEquiv (
    div(float.left, color:="red"),
    """<div style="float: left; color: red;"></div>"""
  )}

  test("Attribute Chaining") { assertXMLEquiv (
    div(`class`:="thing lol", id:="cow"),
    """<div class="thing lol" id="cow"></div>"""
  )}

  test("Mixing Attributes, Styles, and Children") { assertXMLEquiv (
    div(id:="cow", float.left, p("i am a cow")),
    """<div id="cow" style="float: left;"><p>i am a cow</p></div>"""
  )}

  // These two are probably pretty doable, but attrs and styles in frags is gonna really hard
  test("Style after Style-Attribute Appends") { assertXMLEquiv (
    div(cls:="my-class", style:="background-color: red;", float.left, p("i am a cow")),
    """<div class="my-class" style="background-color: red; float: left;"><p>i am a cow</p></div>"""
  )}

  test("Style-Attribute Overwrites Previous Styles") { assertXMLEquiv (
    div(cls:="my-class", float.left, style:="background-color: red;", p("i am a cow")),
    """<div class="my-class" style="background-color: red;"><p>i am a cow</p></div>"""
  )}

  // Implicit conversions save the day
  test("Integer Sequence") { assertXMLEquiv (
    div(h1("Hello ", "#", 1), for(i <- 0 until 5) yield i),
    """<div><h1>Hello #1</h1>01234</div>"""
  )}

  // Very much does not compile right now, I may have to rethink how I do literally everything in order to get shit like
  // this to even compile
  test("String array") { 
    val strArr = Array("hello")
    assertXMLEquiv (
      div(Some("lol"), Some(1), None: Option[String], h1("Hello"), Array(1, 2, 3), strArr, ()),
      """<div>lol1<h1>Hello</h1>123hello</div>"""
    )
  }

  // This is a 'hole' in the syntax that I'm not even interested in plugging, why would you want to do this?
  /*test("Tag apply chaining") { assertXMLEquiv (
    a(tabindex := 1, onclick := "lol")(href := "boo", alt := "g"),
    """<a tabindex="1" onclick="lol" href="boo" alt="g"></a>"""
  )*/

  test("Automatic Pixel Suffixes") {
    assertXMLEquiv (
      div(width:=100, zIndex:=100, height:="100px"),
      """<div style="width: 100px; z-index: 100; height: 100px;"></div>"""
    )
  }

  test("Raw Attributes") { assertXMLEquiv (
    button(
      attrRaw("[class.active]") := "isActive",
      attrRaw("(click)") := "myEvent()"
    ),
    """<button [class.active]="isActive" (click)="myEvent()"></button>"""
  )}

  test("Invalid names fail to compile") {
    assertDoesNotCompile("""div(attr("[(ngModel)]") := "myModel"""", ".*Not a valid XML attribute name.*")
    assertDoesNotCompile("""val s: String = "hi"; div(attr(s) := "myModel"""", ".*Attr name must be a string literal.*")
  }

  test("Repeating Attributes Causes them to be Combined") {
    assertXMLEquiv(
      input(cls := "a", cls := "b").render,
      """<input class="a b" />"""
    )
    /*assertXMLEquiv(
      input(cls := "a")(cls := "b").render,
      """<input class="a b" />"""
    )*/
  }

  test("Big Final Test") { assertXMLEquiv (
    html(
      head(
        script("some script")
      ),
      body(
        h1(backgroundColor:="blue", color:="red", "This is my title"),
        div(backgroundColor:="blue", color:="red",
          p(cls := "contentpara first",
            "This is my first paragraph"
          ),
          a(opacity:=0.9,
            p(cls := "contentpara", "Goooogle")
          )
        )
      )
    ).render, 
    """
    |    <html>
    |        <head>
    |            <script>some script</script>
    |        </head>
    |        <body>
    |            <h1 style="background-color: blue; color: red;">This is my title</h1>
    |            <div style="background-color: blue; color: red;">
    |            <p class="contentpara first">This is my first paragraph</p>
    |            <a style="opacity: 0.9;">
    |                <p class="contentpara">Goooogle</p>
    |            </a>
    |            </div>
    |        </body>
    |    </html>
    """.stripMargin)}

}