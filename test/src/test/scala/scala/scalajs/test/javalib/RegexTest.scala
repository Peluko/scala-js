/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Test Suite        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */
package scala.scalajs.test
package javalib

import scala.scalajs.js
import scala.scalajs.test.JasmineTest
import java.util.regex.Pattern

object RegexTest extends JasmineTest {

  describe("java.util.regex.Pattern") {

    it("should respond to `matches`") {
      expect(Pattern.matches("[Scal]*\\.js", "Scala.js")).toBeTruthy
      expect(Pattern.matches(".[cal]*\\.j.", "Scala.js")).toBeTruthy
      expect(Pattern.matches(".*\\.js", "Scala.js")).toBeTruthy
      expect(Pattern.matches("S[a-z]*", "Scala.js")).toBeFalsy
    }

    it("should respond to `matches` with flags") {
      matches("scala.js", "Scala.js")
      matches("SCALA.JS", "Scala.js")
      matches("waz*up", "WAZZZZZZZZZZZUP")

      def matches(regex: String, input: String): Unit = {
        val result = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input)
        expect(result.matches()).toBeTruthy
      }
    }

    it("should respond to `split`") {
      val result = Pattern.compile("[aj]").split("Scala.js")
      val expected = Array("Sc", "l", ".", "s")
      expect(result.length).toEqual(4)
      expect(result).toEqual(expected)

      // Tests from JavaDoc
      split("boo:and:foo", ":", Array("boo", "and", "foo"))
      split("boo:and:foo", "o", Array("b", "", ":and:f"))

      def split(input: String, regex: String, expected: Array[String]): Unit = {
        val result = Pattern.compile(regex).split(input)
        expect(result).toEqual(expected)
      }
    }

    it("should respond to `split` with limit") {
      // Tests from JavaDoc
      splitWithLimit("boo:and:foo", ":", 2, Array("boo", "and:foo"))
      splitWithLimit("boo:and:foo", ":", 5, Array("boo", "and", "foo"))
      splitWithLimit("boo:and:foo", ":", -2, Array("boo", "and", "foo"))
      splitWithLimit("boo:and:foo", "o", 5, Array("b", "", ":and:f", "", ""))
      splitWithLimit("boo:and:foo", "o", -2, Array("b", "", ":and:f", "", ""))
      splitWithLimit("boo:and:foo", "o", 0, Array("b", "", ":and:f"))

      def splitWithLimit(input: String, regex: String, limit: Int, expected: Array[String]): Unit = {
        val result = Pattern.compile(regex).split(input, limit)
        expect(result).toEqual(expected)
      }
    }

    it("should respond to `flags`") {
      val pattern0 = Pattern.compile("a")
      val pattern1 = Pattern.compile("a", 0)
      val flags2 = Pattern.CASE_INSENSITIVE | Pattern.DOTALL
      val pattern2 = Pattern.compile("a", flags2)

      expect(pattern0.flags).toEqual(0)
      expect(pattern1.flags).toEqual(0)
      expect(pattern2.flags).toEqual(flags2)
    }

    it("should respond to `pattern` and `toString`") {
      def checkPatternAndToString(regex: String): Unit = {
        val pattern0 = Pattern.compile(regex)
        expect(pattern0.pattern).toEqual(regex)
        expect(pattern0.toString).toEqual(regex)

        val pattern1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        expect(pattern1.pattern).toEqual(regex)
        expect(pattern1.toString).toEqual(regex)
      }

      checkPatternAndToString("a*b+c")
      checkPatternAndToString("\\S[(a1]a.js")
    }

    it("should respond to `quote`") {
      val splitWithQuote = Pattern.compile(Pattern.quote("$1&$2")).split("Scala$1&$2.js")
      val splitNoQuote = Pattern.compile("$1&$2").split("Scala$1&$2.js")
      expect(splitWithQuote.mkString).toEqual("Scala.js")
      expect(splitNoQuote.mkString).toEqual("Scala$1&$2.js")
    }

  }

  describe("java.util.regex.Matcher") {

    it("should respond to `find`") {
      val matcher = Pattern.compile("a").matcher("Scala.js")

      expect(matcher.find()).toBeTruthy
      expect(matcher.find()).toBeTruthy
      expect(matcher.find()).toBeFalsy
      expect(matcher.find(4)).toBeTruthy
      expect(matcher.find()).toBeFalsy
    }

    it("should respond to `start`, `end`, `group`, and `toMatchResult`") {
      val matcher = Pattern.compile("\\s(([A-Za-z]{5}).js)\\s").matcher("Write Scala.js everyday!")

      def checkGroup0(start: Int, end: Int, group: String) =
        checkGroup(start, 5, end, 15, group, " Scala.js ")

      def checkGroup1(start: Int, end: Int, group: String) =
        checkGroup(start, 6, end, 14, group, "Scala.js")

      def checkGroup2(start: Int, end: Int, group: String) =
        checkGroup(start, 6, end, 11, group, "Scala")

      def checkGroup(start: Int, startExpected: Int, end: Int, endExpected: Int,
                     group: String, groupExpected: String): Unit = {
        expect(start).toBe(startExpected)
        expect(end).toBe(endExpected)
        expect(group).toBe(groupExpected)
      }

      expect(matcher.find()).toBeTruthy
      expect(matcher.groupCount).toEqual(2)
      checkGroup0(matcher.start, matcher.end, matcher.group)
      checkGroup0(matcher.start(0), matcher.end(0), matcher.group(0))
      checkGroup1(matcher.start(1), matcher.end(1), matcher.group(1))
      checkGroup2(matcher.start(2), matcher.end(2), matcher.group(2))

      val matchResult = matcher.toMatchResult
      expect(matchResult.groupCount).toEqual(2)
      checkGroup0(matchResult.start, matchResult.end, matchResult.group)
      checkGroup0(matchResult.start(0), matchResult.end(0), matchResult.group(0))
      checkGroup1(matchResult.start(1), matchResult.end(1), matchResult.group(1))
      checkGroup2(matchResult.start(2), matchResult.end(2), matchResult.group(2))
    }

    it("should respond to `matches`") {
      val matcher0 = Pattern.compile("S[a-z]+").matcher("Scala")
      val matcher1 = Pattern.compile("S[a-z]+").matcher("Scala.js")

      expect(matcher0.matches()).toBeTruthy
      expect(matcher1.matches()).toBeFalsy
    }

    it("should respond to `reset`") {
      val matcher = Pattern.compile("S[a-z]+").matcher("Scalable")

      expect(matcher.find()).toBeTruthy
      expect(matcher.find()).toBeFalsy
      matcher.reset()
      expect(matcher.find()).toBeTruthy
    }

    it("should respond to `reset(String)`") {
      val matcher = Pattern.compile("S[a-z]+").matcher("Scalable")

      expect(matcher.matches()).toBeTruthy
      matcher.reset("Scala.js")
      expect(matcher.matches()).toBeFalsy
    }

    it("should respond to `usePattern`") {
      val patternNoDots = Pattern.compile("S[a-z]+")
      val patternWithDots = Pattern.compile("S[a-z.]+")

      val matcher0 = patternNoDots.matcher("Scala.js")
      expect(matcher0.matches()).toBeFalsy
      matcher0.usePattern(patternWithDots)
      expect(matcher0.matches()).toBeTruthy

      val matcher1 = patternWithDots.matcher("Scala.js")
      expect(matcher1.matches()).toBeTruthy
      matcher1.usePattern(patternNoDots)
      expect(matcher1.matches()).toBeFalsy
    }

    it("should respond to `lookingAt`") {
      val matcher0 = Pattern.compile("S[a-z]+").matcher("Scala")
      val matcher1 = Pattern.compile("S[a-z]+").matcher("Scala.js")
      val matcher2 = Pattern.compile("[a-z]+").matcher("Scala.js")

      expect(matcher0.lookingAt()).toBeTruthy
      expect(matcher1.lookingAt()).toBeTruthy
      expect(matcher2.lookingAt()).toBeFalsy

      val matcher3 = Pattern.compile("S[a-z]+").matcher("Scala.js")
      expect(matcher3.find()).toBeTruthy
      expect(matcher3.lookingAt()).toBeTruthy
    }

    it("should respond to `hitEnd`") {
      val matcher0 = Pattern.compile("S[a-z]*").matcher("Scala.js")
      expect(matcher0.find()).toBeTruthy
      expect(matcher0.hitEnd).toBeFalsy
      expect(matcher0.find()).toBeFalsy
      expect(matcher0.hitEnd).toBeTruthy

      val matcher1 = Pattern.compile("[A-Za-z]+").matcher("Scala.js")
      expect(matcher1.find()).toBeTruthy
      expect(matcher1.hitEnd).toBeFalsy
      expect(matcher1.group).toBe("Scala")
      expect(matcher1.find()).toBeTruthy
      expect(matcher1.hitEnd).toBeTruthy
      expect(matcher1.group).toBe("js")
      expect(matcher1.lookingAt()).toBeTruthy
      expect(matcher1.group).toBe("Scala")
      expect(matcher1.hitEnd).toBeFalsy
    }

    it("should respond to `region`") {
      val matcher0 = Pattern.compile("S[a-z]+").matcher("A Scalable Solution")

      val region0to3 = matcher0.region(0, 3)
      expect(region0to3.regionStart).toBe(0)
      expect(region0to3.regionEnd).toBe(3)
      expect(region0to3.find()).toBeFalsy

      val region0to15 = matcher0.region(0, 15)
      expect(region0to15.regionStart).toBe(0)
      expect(region0to15.regionEnd).toBe(15)
      expect(region0to15.find()).toBeTruthy
      expect(region0to15.group).toEqual("Scalable")

      val region2to7 = region0to15.region(2, 7)
      expect(region2to7.regionStart).toBe(2)
      expect(region2to7.regionEnd).toBe(7)
      expect(region2to7.find()).toBeTruthy
      expect(region2to7.group).toEqual("Scala")

      val region5toEnd = matcher0.region(5, matcher0.regionEnd)
      expect(region5toEnd.regionStart).toBe(5)
      expect(region5toEnd.regionEnd).toBe(19)
      expect(region5toEnd.find()).toBeTruthy
      expect(region5toEnd.group).toEqual("Solution")

      val matcher1 = Pattern.compile("0[xX][A-Fa-f0-9]{3}$").matcher("In CSS, 0xc4fe is not a color")

      val region5to13 = matcher1.region(5, 13)
      expect(region5to13.regionStart).toBe(5)
      expect(region5to13.regionEnd).toBe(13)
      expect(region5to13.find()).toBeTruthy
      expect(region5to13.group).toEqual("0xc4f")

      val region5to20 = matcher1.region(5, 20)
      expect(region5to20.regionStart).toBe(5)
      expect(region5to20.regionEnd).toBe(20)
      expect(region5to20.find()).toBeFalsy
    }

    it("should respond to `appendReplacement` and `appendTail`") {
      // From the JavaDoc
      val matcher = Pattern.compile("cat").matcher("one cat two cats in the yard")
      val sb = new StringBuffer

      while (matcher.find()) {
        matcher.appendReplacement(sb, "dog")
      }
      matcher.appendTail(sb)

      expect(sb.toString).toBe("one dog two dogs in the yard")
    }

    it("should respond to `replaceAll`") {
      // From the JavaDoc
      val matcher = Pattern.compile("a*b").matcher("aabfooaabfooabfoob")
      expect(matcher.replaceAll("-")).toBe("-foo-foo-foo-")
    }

    it("should respond to `replaceFirst`") {
      // From the JavaDoc
      val matcher = Pattern.compile("dog").matcher("zzzdogzzzdogzzz")
      expect(matcher.replaceFirst("cat")).toBe("zzzcatzzzdogzzz")
    }

    it("should throw exception if match accessors are called before `find`") {
      def checkInvalidAccess(block: => Unit): Unit = {
        val exception: Throwable = try {
          block
          throw new Error("No exception thrown")
        } catch {
          case e: Throwable => e
        }

        expect(exception.getClass.getName).toBe("java.lang.IllegalStateException")
        expect(exception.getMessage).toBe("No match available")
      }

      val matcher = Pattern.compile("(Sc([a-z]*))").matcher("Scala.js")

      checkInvalidAccess { matcher.start }
      checkInvalidAccess { matcher.end }
      checkInvalidAccess { matcher.group }
      checkInvalidAccess { matcher.group(42) }

      val matchResult = matcher.toMatchResult

      checkInvalidAccess { matchResult.start }
      checkInvalidAccess { matchResult.end }
      checkInvalidAccess { matchResult.group }
      checkInvalidAccess { matchResult.group(42) }
    }

  }
}
