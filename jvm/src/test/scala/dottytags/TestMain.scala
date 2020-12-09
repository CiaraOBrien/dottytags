package dottytags

import scalatags.Text.all._

object Main {

  def main(args: Array[String]): Unit = {
    assert(Escape.isValidTag(capitalize("abc1_")))
    //assert(Escape.isValidTag("abc1."))
    assert(Escape.isValidTag("abc1_" + System.currentTimeMillis.toString))
    //assert(Escape.isValidTag("abc1." + System.currentTimeMillis.toString))

    println(div(
      attr("data-app-key") := "YOUR_APP_KEY",
      css("background-color") := "red",
      attr("bruh") := "YOUR_APP_KEY",
      css("bruh-color") := "red"
    ).render)
  }

}