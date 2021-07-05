import dottytags.*
import dottytags.predefs.all.*

object Playground {
  def main(args: Array[String]): Unit = {
    println(html(cls := "foo", href := "bar", css("baz1") := "qux", "quux",
      System.currentTimeMillis.toString, css("baz2") := "qux", raw("a")
    ).toString)
  }
}