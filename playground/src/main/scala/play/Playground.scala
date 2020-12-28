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
    val numVisitors: Int = 1023
    val posts: Seq[(String, String)] = Seq(
      ("alice", "i like pie"),
      ("bob", "pie is evil i hate you"),
      ("charlie", "i like pie and pie is evil, i hat myself")
    )
    val bold: String = System.currentTimeMillis.toString

    println(html(
      head(
        script("some script")

      ),
      body(
        h1("This is my <<<< title", cssPx("weight") := System.currentTimeMillis.toString),
        div("posts"),
        for ((name, text) <- posts) yield div(
            h2("Post by ", name),
            p(text)
          ),
        frag(raw("<"), ">"),
        cls := "test",
        if numVisitors > 100 then p("No more posts!")
        else p("Please post below...")
    )).render)