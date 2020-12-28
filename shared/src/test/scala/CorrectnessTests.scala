import minitest._
import dottytags._
import dottytags.implicits.given
import dottytags.tags._
//import dottytags.svg._
import dottytags.attrs._
import dottytags.styles._

import scala.language.implicitConversions

object CorrectnessTests extends SimpleTestSuite {
  test("Basic tree building") {
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
  test("CSS style chaining") { assertXMLEquiv (
    div(float.left, color:="red"),
    """<div style="float: left; color: red;"></div>"""
  )}
  test("Attribute chaining") { assertXMLEquiv (
    div(`class`:="thing lol", id:="cow"),
    """<div class="thing lol" id="cow"></div>"""
  )}
  test("Mixing attributes, styles, and children") { assertXMLEquiv (
    div(id:="cow", float.left, p("i am a cow")),
    """<div id="cow" style="float: left;"><p>i am a cow</p></div>"""
  )}
  // These two are probably pretty doable, but attrs and styles in frags is gonna really hard
  test("Style after style-attribute appends") { assertXMLEquiv (
    div(cls:="my-class", style:="background-color: red;", float.left, p("i am a cow")),
    """<div class="my-class" style="background-color: red; float: left;"><p>i am a cow</p></div>"""
  )}
  test("Style-attribute overwrites previous styles") { assertXMLEquiv (
    div(cls:="my-class", float.left, style:="background-color: red;", p("i am a cow")),
    """<div class="my-class" style="background-color: red;"><p>i am a cow</p></div>"""
  )}
  // Implicit conversions save the day
  /*test("Integer sequence") { assertXMLEquiv (
    div(h1("Hello ", "#", 1), for(i <- 0 until 5) yield i),
    """<div><h1>Hello #1</h1>01234</div>"""
  )}*/
  // Very much does not compile right now, I may have to rethink how I do literally everything in order to get shit like
  // this to even compile
  /*test("String array") { 
    val strArr = Array("hello")
    assertXMLEquiv (
      div(Some("lol"), Some(1), None: Option[String], h1("Hello"), Array(1, 2, 3), strArr, ()),
      """<div>lol1<h1>Hello</h1>123hello</div>"""
    )
  }*/
  // This is a 'hole' in the syntax that I'm not even interested in plugging, why would you want to do this?
  /*test("Tag apply chaining") { assertXMLEquiv (
    a(tabindex := 1, onclick := "lol")(href := "boo", alt := "g"),
    """<a tabindex="1" onclick="lol" href="boo" alt="g"></a>"""
  )*/
  /*test("Automatic pixel suffixes which do not double up when a pixel suffix is already present") {
    assertXMLEquiv (
      div(width:=100, zIndex:=100, height:="100px"),
      """<div style="width: 100px; z-index: 100; height: 100px;"></div>"""
    )
  }*/
}