package test

//import scalatags.Text.all._
import dottytags.Core._
import dottytags.Tags._

object Main {

  def main(args: Array[String]): Unit = {
    //println(tag("html")())
    //println(tag("bruh."))
    //println(tag("bruh" + System.currentTimeMillis.toString))
    //println(print(attr("class") := ("bruh" + System.currentTimeMillis.toString)))
    //println(print(css("background-color") := "red"))
    //println(print(cssPx("background-color") := "red"))

    //tagSelfClosing("bruh")(Seq(attr("class") := "bruh", css("color") := "red"), cssPx("height") := "100")

    html(attr("class") := "bruh", css("href") := "lol", css("wow") := "wowie", "bruh", System.currentTimeMillis.toString, raw("<"))

    val numVisitors = 1023
      val posts = Seq(
        ("alice", "i like pie"),
        ("bob", "pie is evil i hate you"),
        ("charlie", "i like pie and pie is evil, i hat myself")
      )

      println(html(
        head(
          script("some script")

        ),
        body(
          h1("This is my <<<< title", css("weight") := "bold"),
          div("posts"),
          bind(for ((name, text) <- posts) yield div(
            h2("Post by ", name),
            p(text)
          )),
          attr("class") := "test",
          if numVisitors > 100 then p("No more posts!")
          else p("Please post below...")
      )))

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