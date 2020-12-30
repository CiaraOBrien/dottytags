package play

import dottytags._
import dottytags.utils.implicits.given
import scala.language.implicitConversions
import dottytags.predefs.tags._
import dottytags.predefs.attrs._
import dottytags.predefs.styles._

object Playground:
  def main(args: Array[String]): Unit =
    val strArr = Array("hello")
    div(Some("lol"), Some(1), None: Option[String], h1("Hello"), Array(1, 2, 3), strArr, ())
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
        h1("This is my <<<< title", css("weight") := System.currentTimeMillis.toString),
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