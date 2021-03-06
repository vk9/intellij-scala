package org.jetbrains.plugins.scala.lang.formatter.tests

import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import org.jetbrains.plugins.scala.ScalaLanguage
import org.jetbrains.plugins.scala.lang.formatter.AbstractScalaFormatterTestBase
import org.jetbrains.plugins.scala.lang.formatting.settings.ScalaCodeStyleSettings

class ScalaWrappingAndBracesTest extends AbstractScalaFormatterTestBase {
  private val RightMarginMarker = "!"
  private val CHOP_DOWN_IF_LONG = CommonCodeStyleSettings.WRAP_AS_NEEDED | CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM

  private def setupRightMargin(rightMarginVisualHelper: String): Unit = {
    getSettings.setRightMargin(ScalaLanguage.INSTANCE, rightMarginVisualHelper.indexOf(RightMarginMarker))
  }

  def testInfixExpressionWrapAsNeeded() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2
        |2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: 2 :: Nil
        |2 + 2 + 2 + 2 + 22 * 66 + 2
        |""".stripMargin
    val after =
      """2 + 2 + 2 + 2 + 2 +
        |  2 + 2 + 2 + 2 +
        |  2 + 2 + 2 + 2 +
        |  2 + 2
        |2 :: 2 :: 2 :: 2 ::
        |  2 :: 2 :: 2 ::
        |  2 :: 2 :: 2 ::
        |  2 :: 2 :: Nil
        |2 + 2 + 2 + 2 +
        |  22 * 66 + 2
        |""".stripMargin
    doTextTest(before, after)
  }

  def testInfixPatternWrapAsNeeded() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """List(1, 2) match {
        |  case x :: y :: z :: Nil =>
        |}
        |""".stripMargin
    val after =
      """List(1, 2) match {
        |  case x :: y ::
        |    z :: Nil =>
        |}
        |""".stripMargin
    doTextTest(before, after)
  }

  def testInfixTypeWrapAsNeeded() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """val x: T + T + T + T + T
        |""".stripMargin
    val after =
      """val x: T + T + T +
        |  T + T
        |""".stripMargin
    doTextTest(before, after)
  }

  def testInfixExprWrapAlways() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """2 + 3 + 4 * 6 + (7 + 9 * 10) - 8 - 4
        |""".stripMargin
    val after =
      """2 +
        |  3 +
        |  4 *
        |    6 +
        |  (7 +
        |    9 *
        |      10) -
        |  8 -
        |  4
        |""".stripMargin
    doTextTest(before, after)
  }

  def testInfixExprWrapAllIfLong() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """2 + 2 + 2 + 2 + 2 + 2
        |2 + 2 + 2 + 2 + 2
        |""".stripMargin
    val after =
      """2 +
        |  2 +
        |  2 +
        |  2 +
        |  2 +
        |  2
        |2 + 2 + 2 + 2 + 2
        |""".stripMargin
    doTextTest(before, after)
  }

  def testInfixExprDoNotWrap() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    getSettings.setRightMargin(null, 20)
    getIndentOptions.CONTINUATION_INDENT_SIZE = 2
    val before =
      """2 + 2 + 2 + 2 + 2 + 2
        |2 + 2 + 2 + 2 + 2
        |""".stripMargin
    val after =
      """2 + 2 + 2 + 2 + 2 + 2
        |2 + 2 + 2 + 2 + 2
        |""".stripMargin
    doTextTest(before, after)
  }

  def testAlignBinary() {
    getCommonSettings.ALIGN_MULTILINE_BINARY_OPERATION = true
    val before =
      """val i = 2 + 2 +
        | 3 + 5 +
        | 6 + 7 *
        | 8
        |""".stripMargin
    val after =
      """val i = 2 + 2 +
        |        3 + 5 +
        |        6 + 7 *
        |            8
        |""".stripMargin
    doTextTest(before, after)
  }

  def testBinaryParentExpressionWrap() {
    getCommonSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    getCommonSettings.PARENTHESES_EXPRESSION_LPAREN_WRAP = true
    getCommonSettings.PARENTHESES_EXPRESSION_RPAREN_WRAP = true
    getSettings.setRightMargin(null, 20)
    val before =
      """(2333333333333333 + 2)
        |(2 +
        |2)
        |""".stripMargin
    val after =
      """(
        |  2333333333333333 +
        |    2
        |  )
        |(
        |  2 +
        |    2
        |  )
        |""".stripMargin
    doTextTest(before, after)
  }

  def testCallParametersWrap() {
    getCommonSettings.CALL_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    val before =
      """foo(1, 2, 3)
        |""".stripMargin
    val after =
      """foo(1,
        |  2,
        |  3)
        |""".stripMargin
    doTextTest(before, after)
  }

  def testAlignMultilineParametersCalls() {
    getCommonSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = true
    val before =
      """foo(1,
        |2,
        |3)
        |""".stripMargin
    val after =
      """foo(1,
        |    2,
        |    3)
        |""".stripMargin
    doTextTest(before, after)
  }

  def testCallParametersParen() {
    getScalaSettings.CALL_PARAMETERS_NEW_LINE_AFTER_LPAREN = ScalaCodeStyleSettings.NEW_LINE_ALWAYS
    getCommonSettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE = true
    val before =
      """foo(1,
        |2,
        |3)
        |""".stripMargin
    val after =
      """foo(
        |  1,
        |  2,
        |  3
        |)
        |""".stripMargin
    doTextTest(before, after)
  }

  def testBraceStyle() {
    getCommonSettings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE
    getCommonSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_SHIFTED
    getCommonSettings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED
    val before =
      """class A {
        |  def foo = {
        |  val z =
        |  {
        |   3
        |  }
        |  }
        |}
        |class B extends A {
        |}
        |""".stripMargin
    val after =
      """class A
        |{
        |  def foo =
        |    {
        |    val z =
        |    {
        |      3
        |    }
        |    }
        |}
        |
        |class B extends A
        |{
        |}
        |""".stripMargin
    doTextTest(before, after)
  }


  //
  // Chained method calls
  //
  // Reminder about settings combinations:
  // (vertical align / do not align)
  // (no wrap / wrap if long / chop if long / wrap always )
  // (wrap first / no wrap first) # only for "wrap always" and "chop if long"
  // TODO: add tests on ENTER press, calculate correct indent for these special cases

  //
  // Do not align when multiline
  //

  def test_MethodCallChain_DoNotWrap() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)"""
    doTextTest(before)
  }

  def test_MethodCallChain_DoNotWrap_1() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2)
        |.bar(3, 4)
        |.baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapIfLong() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_ChopDownIfLong() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_ChopDownIfLong_WrapFirstCall() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapAlways() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapAlways_WrapFirstCall() {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  //
  // Align when multiline
  //

  def test_MethodCallChain_Align_DoNotWrap() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |        .baz(5, 6).foo(7, 8)
        |        .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_FirstCallOnNewLine() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject
        |.foo(1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapIfLong() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    setupRightMargin(
      """                              ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).looooooongMethod(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |        .baz(5, 6).foo(7, 8)
        |        .bar(9, 10)
        |        .looooooongMethod(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_ChopDownIfLong() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    setupRightMargin(
      """                                ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |        .bar(3, 4)
        |        .baz(5, 6)
        |        .foo(7, 8)
        |        .bar(9, 10)
        |        .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_ChopDownIfLong_WrapFirstCall() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapAlways() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |        .foo(1, 2)
        |        .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapAlways_WrapFirstCall() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCall() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = false
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """foo[T1, T2](1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """foo[T1, T2](1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCall_Align() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """foo[T1, T2](1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """foo[T1, T2](1, 2).bar(3, 4)
        |                 .baz(5, 6).foo(7, 8)
        |                 .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  // this is some legacy peculiar case when first dot in method call chain starts in on previous line
  // still this does not work properly with wrapping styles different from NO_WRAP
  def test_MethodCallChain_Align_FirstDotOnPrevLine() {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    val before =
      """val x = foo.
        |foo.goo.
        |foo(1, 2, 3).
        |foo.
        |foo
        |.foo
        |""".stripMargin
    val after =
      """val x = foo.
        |        foo.goo.
        |        foo(1, 2, 3).
        |        foo.
        |        foo
        |        .foo
        |""".stripMargin
    doTextTest(before, after)
  }
}