package dottytags

import scala.quoted._

private def error(error: String)(using Quotes): Nothing = 
    import quotes.reflect._
    report.error(error)
    throw scala.quoted.runtime.StopMacroExpansion()