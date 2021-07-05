package scala

import minitest.api.{SourceLocation, AssertionException}
import minitest.api.Asserts.*
import Console.*

/*
 * Flatten was adapted from Scalatags.
 */
def flatten(s: String): String = s.replaceAll("(\\n|\\s)+<", "<").nn.trim.nn

extension (s: String) def takePadding(n: Int, sep: String = ""): String = 
  val take = s.take(n)
  if take.length == n then take else take + sep + " " + (" " * (n - take.length))

extension (e: AssertionException) def elideStackTrace: AssertionException = {
  e.setStackTrace(Array.empty[java.lang.StackTraceElement | Null])
  e
}

def assertXMLEquiv(r: Any, e: String)(using pos: SourceLocation): Unit = 
  val rf = flatten(r.toString); val ef = flatten(e)
  val matchLen = rf.zip(ef).segmentLength(_ == _, 0)
  val barLen   = 100
  val before   = Math.min(matchLen, barLen / 2)
  val after    = Math.min(Math.max(rf.length, ef.length), barLen)
  val response = 
  s"""$WHITE$BOLD|Mismatch$RESET at
     |$WHITE$BOLD|$RESET${rf.drop(matchLen - before).take(before)}$WHITE$BOLD${rf.lift(matchLen).getOrElse(s"$RED_B ")}$RESET${rf.drop(matchLen + 1).takePadding(barLen - before)}$WHITE$BOLD|
     |$WHITE$BOLD|$RESET$GREEN${"|" * before}$RED_B$WHITE$BOLD|$RESET$RED$RED_B${rf.drop(matchLen + 1).takePadding(barLen - before, RESET)}$BLACK_B$WHITE$BOLD|
     |$WHITE$BOLD|$RESET${ef.drop(matchLen - before).take(before)}$WHITE$BOLD${ef.lift(matchLen).getOrElse(s"$GREEN_B ")}$RESET${ef.drop(matchLen + 1).takePadding(barLen - before)}$WHITE$BOLD|""".stripMargin
  if rf.length != ef.length || matchLen != rf.length then throw AssertionException(response, pos).elideStackTrace