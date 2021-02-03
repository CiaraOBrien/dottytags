package play

import dottytags._
  
object Playground {
  def main(args: Array[String]): Unit = {
    val s: String = "hi"
    div(attr(s) := "myModel")
    div(attr("[(ngModel)]") := "myModel")
  }
}