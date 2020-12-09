package test

//import scalatags.Text.all._
import dottytags.Core._

object Main {

  def main(args: Array[String]): Unit = {
    //println(tag("html")())
    //println(tag("bruh."))
    //println(tag("bruh" + System.currentTimeMillis.toString))
    //println(print(attr("class") := ("bruh" + System.currentTimeMillis.toString)))
    //println(print(css("background-color") := "red"))
    //println(print(cssPx("background-color") := "red"))

    println(
      tag("html")(
        tag("head")(
          tag("script")("console.log(1)")
        ),
        tag("body")(
          tag("h1")(css("color") := "red", "title"),
          tag("div")(css("backgroundColor") := "blue",
            tag("a")(attr("href") := "www.google.com", 
              tag("p")("Goooogle")
            )
          )
        )
      )
    )

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