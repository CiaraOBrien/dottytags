package test

import scalatags.Text.all._
/*import dottytags._
import dottytags.utils.implicits.given
import scala.language.implicitConversions
import dottytags.predefs.tags._
import dottytags.predefs.attrs._
import dottytags.predefs.styles._
import dottytags.predefs.svg._*/

object Main {

  def main(args: Array[String]): Unit = {
    /*println(html(cls := "foo", href := "bar", clipPathBind := "qux", "quux", 
      borderSpacing := ("horiz", "vert"), css("baz2") := "qux", clipPath("a")
    ).render)*/

    val numVisitors: Int = 1023
    val posts: Seq[(String, String)] = Seq(
      ("alice", "i like pie"),
      ("bob", "pie is evil i hate you"),
      ("charlie", "i like pie and pie is evil, i hat myself")
    )
    val bold: String = System.currentTimeMillis.toString

    def calc(): String = html(
      head(
        script("some script")
      ),
      body(
        h1("This is my <<<< title", css("weight") := bold),
        div("posts"),
        for ((name, text) <- posts) yield div(
            h2("Post by ", name),
            p(text)
          ),
        frag(raw("<"), ">"),
        cls := "test",
        if numVisitors > 100 then p("No more posts!")
        else p("Please post below...")
    )).render

    val quux = "quux"
    //def calc() = html(cls := "foo", href := "bar", backgroundAttachment.fixed, quux, css("baz2") := "qux", raw("a")).render

    println(calc())

    val start = System.currentTimeMillis()
        var i = 0
        val d = 10000

        while(System.currentTimeMillis() - start < d) do {
          i += 1
          calc()
        }

        println(i + " in " + d)

    //println(calc())

    //assert(capitalize("abc1_") == "ABC1_")
    //assert(Escape.isValidTag("abc1."))
    //assert(Escape.isValidTag("abc1_" + System.currentTimeMillis.toString))
    //assert(Escape.isValidTag("abc1." + System.currentTimeMillis.toString))


    //<div data-app-key="YOUR_APP_KEY" bruh="YOUR_APP_KEY" style="background-color: red; bruh-color: red;"/>
    //<div data-app-key="YOUR_APP_KEY" style="background-color: red; bruh-color: red;" bruh="YOUR_APP_KEY"></div>
    /*println(tag("div")(
      attr("data-app-key") := "YOUR_APP_KEY",
      css("background-color") := "red",
      attr("bruh") := "YOUR_APP_KEY",
      css("bruh-color") := "red",
    ))*/
  }

}