import minitest.*
import dottytags.*
import dottytags.utils.syntax.given
import dottytags.utils.cssUnits.*
import scala.language.implicitConversions
import dottytags.predefs.tags.*
import dottytags.predefs.attrs.*
import dottytags.predefs.styles.*

/*
 * Most of these were adapted from Scalatags' test suite so as to correctly test for compatibility.
 * (see LICENSE for copyright notice)
 */

object ExampleTests extends SimpleTestSuite {

  test("Import Example") { assertXMLEquiv(
      div(
        p(color:="red", fontSize:=64.pt, "Big Red Text"),
        img(href:="www.imgur.com/picture.jpg")
      ),
      """
      |    <div>
      |        <p style="color: red; font-size: 64pt;">Big Red Text</p>
      |        <img href="www.imgur.com/picture.jpg" />
      |    </div>
      """.stripMargin
    )
  }

  test("Splash Example") { assertXMLEquiv (
    html(
        head(
          script(src:="..."),
          script(
            "alert('Hello World')"
          )
        ),
        body(
          div(
            h1(id:="title", "This is a title"),
            p("This is a big paragraph of text")
          )
        )
      )
      ,
      """
      <html>
          <head>
              <script src="..."></script>
              <script>alert('Hello World')</script>
          </head>
          <body>
              <div>
                  <h1 id="title">This is a title</h1>
                  <p>This is a big paragraph of text</p>
              </div>
          </body>
      </html>
      """  
  )}

  test("Hello World") { assertXMLEquiv (
    html(
        head(
          script("some script")
        ),
        body(
          h1("This is my title"),
          div(
            p("This is my first paragraph"),
            p("This is my second paragraph")
          )
        )
      )
      ,
      """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1>This is my title</h1>
                <div>
                    <p>This is my first paragraph</p>
                    <p>This is my second paragraph</p>
                </div>
            </body>
        </html>
      """
  )}

  test("Variables") { assertXMLEquiv (
    {
      val title = "title"
      val numVisitors = 1023

      html(
        head(
          script("some script")
        ),
        body(
          h1("This is my ", title),
          div(
            p("This is my first paragraph"),
            p("you are the ", numVisitors.toString, "th visitor!")
          )
        )
      )
    }
    ,
    """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1>This is my title</h1>
                <div>
                    <p>This is my first paragraph</p>
                    <p>you are the 1023th visitor!</p>
                </div>
            </body>
        </html>
    """
  )}

  test("Control Flow") { assertXMLEquiv (
    {
      val numVisitors = 1023
      val posts = Seq(
        ("alice", "i like pie"),
        ("bob", "pie is evil i hate you"),
        ("charlie", "i like pie and pie is evil, i hat myself")
      )

      html(
        head(
          script("some script")
        ),
        body(
          h1("This is my title"),
          div("posts"),
          for ((name, text) <- posts) yield div(
            h2("Post by ", name),
            p(text)
          ),
          if numVisitors > 100 then p("No more posts!")
          else p("Please post below...")
        )
      )
    }
    ,
    """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1>This is my title</h1>
                <div>posts</div>
                <div>
                    <h2>Post by alice</h2>
                    <p>i like pie</p>
                </div>
                <div>
                    <h2>Post by bob</h2>
                    <p>pie is evil i hate you</p>
                </div>
                <div>
                    <h2>Post by charlie</h2>
                    <p>i like pie and pie is evil, i hat myself</p>
                </div>
                <p>No more posts!</p>
            </body>
        </html>
    """
  )}

  test("Functions") { assertXMLEquiv ( {
      def imgBox(source: String, text: String) = div(
        img(src:=source),
        div(
          p(text)
        )
      )

      html(
        head(
          script("some script")
        ),
        body(
          h1("This is my title"),
          imgBox("www.mysite.com/imageOne.png", "This is the first image displayed on the site"),
          div(`class`:="content",
            p("blah blah blah i am text"),
            imgBox("www.mysite.com/imageTwo.png", "This image is very interesting")
          )
        )
      )
    }
    ,
    """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1>This is my title</h1>
                <div>
                    <img src="www.mysite.com/imageOne.png" />
                    <div>
                        <p>This is the first image displayed on the site</p>
                    </div>
                </div>
                <div class="content">
                    <p>blah blah blah i am text</p>
                    <div>
                        <img src="www.mysite.com/imageTwo.png" />
                    <div>
                        <p>This image is very interesting</p>
                    </div>
                    </div>
                </div>
            </body>
        </html>
    """
  )}

  test("Attributes") { assertXMLEquiv (
     html(
        head(
          script("some script")
        ),
        body(
          h1("This is my title"),
          div(
            p(attr("onclick") := "... do some js",
              "This is my first paragraph"
            ),
            a(attr("href") := "www.google.com",
              p("Goooogle")
            ),
            p(attr("hidden").empty,
              "I am hidden"
            )
          )
        )
      )
      ,
      """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1>This is my title</h1>
                <div>
                    <p onclick="... do some js">This is my first paragraph</p>
                    <a href="www.google.com">
                        <p>Goooogle</p>
                    </a>
                    <p hidden="hidden">I am hidden</p>
                </div>
            </body>
        </html>
      """
  )}

  test("Custom Classes and CSS") { assertXMLEquiv (
    html(
        head(
          script("some script")
        ),
        body(
          h1(style:="background-color: blue; color: red;", "This is my title"),
          div(style:="background-color: blue; color: red;",
            p(`class`:="contentpara first", 
              "This is my first paragraph"
            ),
            a(style:="opacity: 0.9;",
              p(cls := "contentpara", "Goooogle")
            )
          )
        )
      )
      ,
      """
        <html>
            <head>
                <script>some script</script>
            </head>
            <body>
                <h1 style="background-color: blue; color: red;">This is my title</h1>
                <div style="background-color: blue; color: red;">
                <p class="contentpara first">This is my first paragraph</p>
                <a style="opacity: 0.9;">
                    <p class="contentpara">Goooogle</p>
                </a>
                </div>
            </body>
        </html>
      """
  )}

  test("Non-String Attribute And Styles") { assertXMLEquiv (
    div(
        p(float.left,
          "This is my first paragraph"
        ),

        a(tabindex:=10,
          p("Goooogle")
        ),
        input(attr("disabled"):=true)
      )
      ,
      """
        <div>
            <p style="float: left;">This is my first paragraph</p>
            <a tabindex="10">
                <p>Goooogle</p>
            </a>
            <input disabled="true" />
        </div>
      """
  )}

  test("Boolean Attributes") { assertXMLEquiv (
      div(input(readonly)), """<div><input readonly="readonly" /></div>"""
    )
  }

  test("Layouts") { assertXMLEquiv ( {
      def page(scripts: Seq[Tag], content: Seq[Tag]): String = 
        html(
          head(scripts),
          body(
            h1("This is my title"),
            div(cls := "content", content)
          )
        ).render

      page(
        Seq(
          script("some script")
        ),
        Seq(
          p("This is the first ", b("image"), " displayed on the ", a("site")),
          img(src:="www.myImage.com/image.jpg"),
          p("blah blah blah i am text")
        )
      )
    }
    ,
    """
      <html>
          <head>
              <script>some script</script>
          </head>
          <body>
              <h1>This is my title</h1>
                  <div class="content">
                  <p>This is the first <b>image</b> displayed on the <a>site</a></p>
                      <img src="www.myImage.com/image.jpg" />
                  <p>blah blah blah i am text</p>
              </div>
          </body>
      </html>
    """
  )}

}