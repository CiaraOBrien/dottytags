package dottytags

import TagClass._

/*
 * This whole file is, of course, adapted from scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/generic/Tags.scala
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/generic/Tags2.scala
 */

object tags {
  // Root Element
  inline def html       = tag("html")
  // Document Metadata
  inline def head       = tag("head")
  inline def base       = tag("base", sc = true)
  inline def link       = tag("link", sc = true)
  inline def meta       = tag("meta", sc = true)
  // Scripting
  inline def script     = tag("script")
  // Sections
  inline def body       = tag("body")
  inline def h1         = tag("h1")
  inline def h2         = tag("h2")
  inline def h3         = tag("h3")
  inline def h4         = tag("h4")
  inline def h5         = tag("h5")
  inline def h6         = tag("h6")
  inline def header     = tag("header")
  inline def footer     = tag("footer")
  // Grouping content
  inline def p          = tag("p")
  inline def hr         = tag("hr", sc = true)
  inline def pre        = tag("pre")
  inline def blockquote = tag("blockquote")
  inline def ol         = tag("ol")
  inline def ul         = tag("ul")
  inline def li         = tag("li")
  inline def dl         = tag("dl")
  inline def dt         = tag("dt")
  inline def dd         = tag("dd")
  inline def figure     = tag("figure")
  inline def figcaption = tag("figcaption")
  inline def div        = tag("div")
  // Text-level semantics
  inline def a          = tag("a")
  inline def em         = tag("em")
  inline def strong     = tag("strong")
  inline def small      = tag("small")
  inline def s          = tag("s")
  inline def cite       = tag("cite")
  inline def code       = tag("code")
  inline def sub        = tag("sub")
  inline def sup        = tag("sup")
  inline def i          = tag("i")
  inline def b          = tag("b")
  inline def u          = tag("u")
  inline def span       = tag("span")
  inline def br         = tag("br", sc = true)
  inline def wbr        = tag("wbr", sc = true)
  // Edits
  inline def ins        = tag("ins")
  inline def del        = tag("del")
  // Embedded content
  inline def img        = tag("img", sc = true)
  inline def iframe     = tag("iframe")
  inline def embed      = tag("embed", sc = true)
  inline def `object`   = tag("object")
  inline def param      = tag("param", sc = true)
  inline def video      = tag("video")
  inline def audio      = tag("audio")
  inline def source     = tag("source", sc = true)
  inline def track      = tag("track", sc = true)
  inline def canvas     = tag("canvas")
  inline def map        = tag("map")
  inline def area       = tag("area", sc = true)
  // Tabular data
  inline def table      = tag("table")
  inline def caption    = tag("caption")
  inline def colgroup   = tag("colgroup")
  inline def col        = tag("col", sc = true)
  inline def tbody      = tag("tbody")
  inline def thead      = tag("thead")
  inline def tfoot      = tag("tfoot")
  inline def tr         = tag("tr")
  inline def td         = tag("td")
  inline def th         = tag("th")
  // Forms
  inline def form       = tag("form")
  inline def fieldset   = tag("fieldset")
  inline def legend     = tag("legend")
  inline def label      = tag("label")
  inline def input      = tag("input", sc = true)
  inline def button     = tag("button")
  inline def select     = tag("select")
  inline def datalist   = tag("datalist")
  inline def optgroup   = tag("optgroup")
  inline def option     = tag("option")
  inline def textarea   = tag("textarea")
}

/**
  * Contains builtin tags which are used less frequently. These are not imported by
  * default to avoid namespace pollution.
  */
object extraTags {
  // Document metadata
  inline def title      = tag("title")
  inline def style      = tag("style")
  // Scripting
  inline def noscript   = tag("noscript")
  // Sections
  inline def section    = tag("section")
  inline def nav        = tag("nav")
  inline def article    = tag("article")
  inline def aside      = tag("aside")
  inline def address    = tag("address")
  inline def main       = tag("main")
  // Text level semantics
  inline def q          = tag("q")
  inline def dfn        = tag("dfn")
  inline def abbr       = tag("abbr")
  inline def data       = tag("data")
  inline def time       = tag("time")
  inline def `var`      = tag("var")
  inline def samp       = tag("samp")
  inline def kbd        = tag("kbd")
  inline def math       = tag("math")
  inline def mark       = tag("mark")
  inline def ruby       = tag("ruby")
  inline def rt         = tag("rt")
  inline def rp         = tag("rp")
  inline def bdi        = tag("bdi")
  inline def bdo        = tag("bdo")
  // Forms
  inline def keygen     = tag("keygen", sc = true)
  inline def output     = tag("output")
  inline def progress   = tag("progress")
  inline def meter      = tag("meter")
  // Interactive elements
  inline def details    = tag("details")
  inline def summary    = tag("summary")
  inline def command    = tag("command", sc = true)
  inline def menu       = tag("menu")
}