package dottytags

import scala.quoted._
import scala.language.implicitConversions

private def error(error: String)(using Quotes): Nothing = 
    import quotes.reflect._
    report.error(error)
    throw scala.quoted.runtime.StopMacroExpansion()