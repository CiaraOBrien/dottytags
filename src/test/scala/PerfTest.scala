package scala

import minitest.*

object PerfTestDotty extends SimpleTestSuite {

    import dottytags.*
    import dottytags.predefs.all.*
    import dottytags.syntax.given
    import scala.language.implicitConversions

    test("Dotty Perf Test") {


        val start = System.currentTimeMillis()
        var i: Long = 0
        val d = 10000
        var name = "DottyTags"

        while System.currentTimeMillis() - start < d do {
            i += 1
            calcStatic()
        }

        println(name.padTo(20, ' ') + i + " in " + d)
        println(name)

    }

    val titleString = "This is my title"
    val firstParaString = "This is my first paragraph"
    val contentpara = "contentpara"
    val first = "first"

    type AttrType = Attr
    type StyleType = Style

    inline def para(inline n: Int, inline s: String, inline m: AttrType, inline t: StyleType): Tag = p(
        title := ("this is paragraph " + n),
        s,
        m,
        t
    )

    def calcExpensive(): String = {
        html(
            head(
                script("console.log(1)")
            ),
            body(
                h1(color := "red", titleString),
                div(backgroundColor := "blue",
                    para(0,
                        firstParaString,
                        cls := contentpara + " " + first,
                        color := "white"
                    ),
                    a(href := "www.google.com",
                        p("Goooogle")
                    ),
                    bind(for (i <- 0 until 5) yield {
                        para(i,
                            "Paragraph " + i,
                            cls := contentpara,
                            color := (if i % 2 == 0 then "red" else "green"),
                        )
                    })
                )
            )
        ).toString
    }

    def calcCheapish(): String = {
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
        }.toString
    }

    def calcStatic(): String = {
        html(
            head(
                script("some script")
            ),
            body(
                h1(backgroundColor := "blue", color := "red", "This is my title"),
                div(backgroundColor := "blue", color := "red",
                    p(cls := "contentpara first",
                        "This is my first paragraph"
                    ),
                    a(opacity := 0.9,
                        p(cls := "contentpara", "Goooogle")
                    )
                )
            )
        ).toString
    }

}

object PerfTestScala extends SimpleTestSuite {

    import scalatags.*
    import scalatags.Text.*
    import scalatags.Text.all.*

    test("Scala Perf Test") {


        val start = System.currentTimeMillis()
        var i: Long = 0
        val d = 10000
        var name = "ScalaTags"

        while System.currentTimeMillis() - start < d do {
            i += 1
            calcStatic()
        }

        println(name.padTo(20, ' ') + i + " in " + d)
        println(name)

    }

    val titleString = "This is my title"
    val firstParaString = "This is my first paragraph"
    val contentpara = "contentpara"
    val first = "first"

    type AttrType = Text.Modifier
    type StyleType = Text.Modifier

    def para(n: Int, s: String, m: AttrType, t: StyleType) = p(
        title := ("this is paragraph " + n),
        s,
        m,
        t
    )

    def calcExpensive(): String = {
        html(
            head(
                script("console.log(1)")
            ),
            body(
                h1(color := "red", titleString),
                div(backgroundColor := "blue",
                    para(0,
                        firstParaString,
                        cls := contentpara + " " + first,
                        color := "white"
                    ),
                    a(href := "www.google.com",
                        p("Goooogle")
                    ),
                    for (i <- 0 until 5) yield {
                        para(i,
                            "Paragraph " + i,
                            cls := contentpara,
                            color := (if i % 2 == 0 then "red" else "green"),
                        )
                    }
                )
            )
        ).toString
    }

    def calcCheapish(): String = {
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
        }.toString
    }

    def calcStatic(): String = {
        html(
            head(
                script("some script")
            ),
            body(
                h1(backgroundColor := "blue", color := "red", "This is my title"),
                div(backgroundColor := "blue", color := "red",
                    p(cls := "contentpara first",
                        "This is my first paragraph"
                    ),
                    a(opacity := 0.9,
                        p(cls := "contentpara", "Goooogle")
                    )
                )
            )
        ).toString
    }
}