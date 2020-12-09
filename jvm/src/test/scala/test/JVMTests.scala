package dottytags

import utest._
import dottytags.Core._

object JVMTests extends TestSuite {
  val tests = Tests {
    test("isValidTag") {
      //assert(Escape.isValidTag("abc_"))
      //assert(!Escape.isValidTag("abc."))
      //assert(!Escape.isValidTag(capitalize("abc_")))
    }
  }
}

class DottyTagsPerf extends PerfTests {
  import PerfTests._

  inline def para(n: Int, m: Child*) = tag("p")(
    m.appended(attr("title") := ("this is paragraph " + n)): _*
  )

  def calc() = {
    tag("html")(
      tag("head")(
        tag("script")("console.log(1)")
      ),
      tag("body")(
        tag("h1")(css("color") := "red", titleString),
        tag("div")(css("backgroundColor") := "blue",
          para(0,
            attr("class") := contentpara + " " + first,
            firstParaString
          ),
          tag("a")(attr("href") := "www.google.com", 
            tag("p")("Goooogle")
          ),
          for (i <- 0 until 5) yield {
            para(i,
              attr("class") := contentpara,
              css("color") := (if i % 2 == 0 then "red" else "green"),
              "Paragraph ",
              i
            )
          }: _*
        )
      )
    ).toString
  }

}
object PerfTests{
  val titleString = "This is my title"
  val firstParaString = "This is my first paragraph"
  val contentpara = "contentpara"
  val first = "first"
  val expected =
    """
      <html>
        <head>
          <script>console.log(1)</script>
        </head>
        <body>
          <h1 style="color: red;">This is my title</h1>
          <div style="background-color: blue;">
            <p class="contentpara first" title="this is paragraph 0">This is my first paragraph</p>
            <a href="www.google.com">
              <p>Goooogle</p>
            </a>
            <p class="contentpara" style="color: red;" title="this is paragraph 0">Paragraph 0</p>
            <p class="contentpara" style="color: green;" title="this is paragraph 1">Paragraph 1</p>
            <p class="contentpara" style="color: red;" title="this is paragraph 2">Paragraph 2</p>
            <p class="contentpara" style="color: green;" title="this is paragraph 3">Paragraph 3</p>
            <p class="contentpara" style="color: red;" title="this is paragraph 4">Paragraph 4</p>
          </div>
        </body>
      </html>
    """
}
trait PerfTests extends TestSuite{

  def calc(): String
  def name: String = this.toString
  def tests = TestSuite{
    test("correctness"){

      println(calc())
      test("performance"){
        println("Benchmarking " + this.name)
        val start = System.currentTimeMillis()
        var i = 0
        val d = 10000

        while System.currentTimeMillis() - start < d do {
          i += 1
          calc()
        }

        name.padTo(20, ' ') + i + " in " + d

      }
    }
  }
}