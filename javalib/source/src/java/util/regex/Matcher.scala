package java.util.regex

import scala.language.implicitConversions
import scala.annotation.switch
import scala.scalajs.js

final class Matcher private[regex] (
    private var pattern0: Pattern, private var input0: CharSequence,
    private var regionStart0: Int, private var regionEnd0: Int) {

  import Matcher._

  def pattern(): Pattern = pattern0

  // Configuration (updated manually)
  private var regexp = new js.RegExp(pattern0.jspattern, pattern0.jsflags)
  private var inputstr = input0.subSequence(regionStart0, regionEnd0).toString

  // Match result (updated by successful matches)
  private var lastMatch: js.RegExp.ExecResult = null
  private var lastMatchIsValid = false

  // Append state (updated by replacement methods)
  private var appendPos: Int = 0

  // Lookup methods

  def matches(): Boolean = {
    lastMatchIsValid = true
    reset()
    lastMatch = regexp.exec(inputstr)
    if ((lastMatch ne null) && (start != 0 || end != inputstr.length))
      lastMatch = null
    lastMatch ne null
  }

  def lookingAt(): Boolean = {
    lastMatchIsValid = true
    reset()
    lastMatch = regexp.exec(inputstr)
    if ((lastMatch ne null) && (start != 0))
      lastMatch = null
    lastMatch ne null
  }

  def find(): Boolean = {
    lastMatchIsValid = true
    lastMatch = regexp.exec(inputstr)
    lastMatch ne null
  }

  def find(start: Int): Boolean = {
    reset()
    regexp.lastIndex = start
    find()
  }

  // Replace methods

  def appendReplacement(sb: StringBuffer, replacement: String): Matcher = {
    sb.append(inputstr.substring(appendPos, start))

    def isDigit(c: Char) = c >= '0' && c <= '9'

    val len = replacement.length
    var i = 0
    while (i < len) {
      replacement.charAt(i) match {
        case '$' =>
          i += 1
          var j = i
          while (i < len && isDigit(replacement.charAt(i)))
            i += 1
          val group = Integer.parseInt(replacement.substring(j, i))
          sb.append(this.group(group))

        case '\\' =>
          i += 1
          if (i < len)
            sb.append(replacement.charAt(i))
          i += 1

        case c =>
          sb.append(c)
          i += 1
      }
    }

    appendPos = end
    this
  }

  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(inputstr.substring(appendPos))
    appendPos = inputstr.length
    sb
  }

  def replaceFirst(replacement: String): String = {
    reset()

    if (find()) {
      val sb = new StringBuffer
      appendReplacement(sb, replacement)
      appendTail(sb)
      sb.toString
    } else {
      inputstr
    }
  }

  def replaceAll(replacement: String): String = {
    reset()

    val sb = new StringBuffer
    while (find()) {
      appendReplacement(sb, replacement)
    }
    appendTail(sb)

    sb.toString
  }

  // Reset methods

  def reset(): Matcher = {
    regexp.lastIndex = 0
    lastMatch = null
    appendPos = 0
    this
  }

  def reset(input: CharSequence): Matcher = {
    regionStart0 = 0
    regionEnd0 = input.length()
    input0 = input
    inputstr = input0.toString
    reset()
  }

  def usePattern(pattern: Pattern): Matcher = {
    val prevLastIndex = regexp.lastIndex

    pattern0 = pattern
    regexp = new js.RegExp(pattern.jspattern, pattern.jsflags)
    regexp.lastIndex = prevLastIndex
    lastMatch = null
    this
  }

  // Query state methods - implementation of MatchResult

  private def ensureLastMatch: js.RegExp.ExecResult = {
    if (lastMatch == null)
      throw new IllegalStateException("No match available")
    lastMatch
  }

  def groupCount(): Int = ensureLastMatch.length-1

  def start(): Int = ensureLastMatch.index
  def end(): Int = start() + group().length
  def group(): String = ensureLastMatch(0)

  def start(group: Int): Int = {
    if (group == 0) start()
    else {
      val last = ensureLastMatch
      // not provided by JS RegExp, so we make up something that at least
      // will have some sound behavior from scala.util.matching.Regex
      inputstr.indexOf(last(group), last.index)
    }
  }

  def end(group: Int): Int = start(group) + this.group(group).length
  def group(group: Int): String = ensureLastMatch(group)

  // Seal the state

  def toMatchResult(): MatchResult = new SealedResult(inputstr, lastMatch)

  // Other query state methods

  def hitEnd(): Boolean =
    lastMatchIsValid && (lastMatch == null || end() == inputstr.length)

  def requireEnd(): Boolean = ??? // I don't understand the spec

  // Stub methods for region management

  def regionStart(): Int = regionStart0
  def regionEnd(): Int = regionEnd0
  def region(start: Int, end: Int): Matcher =
    new Matcher(pattern0, input0, start, end)

  def hasTransparentBounds(): Boolean = false
  def useTransparentBounds(b: Boolean): Matcher = ???

  def hasAnchoringBounds(): Boolean = true
  def useAnchoringBounds(b: Boolean): Matcher = ???
}

object Matcher {
  // js.Numbers used here are always Ints
  private implicit def jsNumberToInt(i: js.Number): Int = (i: Double).toInt

  def quoteReplacement(s: String): String = {
    var result = ""
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      result += ((c: @switch) match {
        case '\\' | '$' => "\\"+c
        case _ => c
      })
      i += 1
    }
    result
  }

  private final class SealedResult(inputstr: String,
      lastMatch: js.RegExp.ExecResult) extends MatchResult {

    def groupCount(): Int = ensureLastMatch.length-1

    def start(): Int = ensureLastMatch.index
    def end(): Int = start() + group().length
    def group(): String = ensureLastMatch(0)

    def start(group: Int): Int = {
      if (group == 0) start()
      else {
        val last = ensureLastMatch

        // not provided by JS RegExp, so we make up something that at least
        // will have some sound behavior from scala.util.matching.Regex
        inputstr.indexOf(last(group), last.index)
      }
    }

    def end(group: Int): Int = start(group) + this.group(group).length
    def group(group: Int): String = ensureLastMatch(group)

    private def ensureLastMatch: js.RegExp.ExecResult = {
      if (lastMatch == null)
        throw new IllegalStateException("No match available")
      lastMatch
    }
  }
}
