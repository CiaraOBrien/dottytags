package dottytags

import scala.quoted._
import scala.quoted.Unliftable.given

inline def capitalize(inline s: String): String = ${ capitalizeMacro('{s}) }
inline def capitalizeAtRuntime(s: String): String = s.toUpperCase.nn
private def capitalizeMacro(s: Expr[String])(using Quotes): Expr[String] =
	s.unlift.map((str: String) => Expr(str.toUpperCase.nn)).getOrElse('{capitalizeAtRuntime(${s})})

object Escape {

	inline def isValidTag (inline s: String): Boolean = ${ isValidTagMacro('{s}) }
	inline def isValidTagAtRuntime(s: String): Boolean = 
		val len = s.length
		if len == 0 then false else
			val first = s.charAt(0)
			if !(first >= 'a' && first <= 'z') then false else
				var pos = 1
				var valid = true
				while (pos < len && valid) do
					val c = s.charAt(pos)
					valid = (c >= 'a' && c <='z') || (c >= 'A' && c <='Z') || (c >= '0' && c <='9') || 
									 c == '-' || c == ':' || c == '_'
					pos += 1
				valid
	private def isValidTagMacro(s: Expr[String])(using Quotes): Expr[Boolean] = {
		import quotes.reflect._
		s.unlift.map((str: String) => Expr("^[a-z][:\\w0-9-]*$".r.unapplySeq(str).isDefined)).getOrElse('{ 
			Escape.isValidTagAtRuntime(${s}) 
		})
	}

	//inline def isValidTag (s: String) = tagRegex.unapplySeq(s).isDefined
	inline def isValidAttr  (inline s: String): Boolean = ${ isValidAttrMacro('{s}) }
	inline def isValidAttrAtRuntime(s: String): Boolean = 
		val len = s.length
		if len == 0 then false else
			val first = s.charAt(0)
			if !((first >= 'a' && first <='z') || (first >= 'A' && first <='Z') || first ==':') then false else
				var pos = 1
				var valid = true
				while (pos < len && valid) do
					val c = s.charAt(pos)
					valid = (c >= 'a' && c <='z') || (c >= 'A' && c <='Z') || (c >= '0' && c <='9') || 
									 c == '-' || c == ':' || c == '_' || c == '.'
					pos += 1
				valid
	private def isValidAttrMacro(s: Expr[String])(using Quotes): Expr[Boolean] = 
		s.unlift.map((str: String) => Expr("^[a-zA-Z_:][-a-zA-Z0-9_:.]*$".r.unapplySeq(str).isDefined)).getOrElse('{ 
			Escape.isValidTagAtRuntime(${s}) 
		})
	
}
