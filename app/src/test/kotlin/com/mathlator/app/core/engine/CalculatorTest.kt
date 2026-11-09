package com.mathlator.app.core.engine

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for the calculator engine.
 * Covers all golden test cases from the spec (Section 6).
 */
class CalculatorTest {

    private fun eval(expr: String, angle: AngleMode = AngleMode.DEG): EvalResult =
        Calculator.evaluate(expr, angle)

    private fun assertSuccess(expr: String, expected: String, angle: AngleMode = AngleMode.DEG, delta: Double = 1e-9) {
        val result = eval(expr, angle)
        assertTrue("Expected Success for '$expr' but got $result", result is EvalResult.Success)
        val actual = (result as EvalResult.Success).value.toDouble()
        val exp = expected.toDouble()
        assertEquals("'$expr' expected $expected but got $actual", exp, actual, delta)
    }

    private fun assertError(expr: String, expected: EvalError, angle: AngleMode = AngleMode.DEG) {
        val result = eval(expr, angle)
        assertTrue("Expected Error($expected) for '$expr' but got $result", result is EvalResult.Error)
        assertEquals(expected, (result as EvalResult.Error).type)
    }

    // ── Golden test cases (Section 6) ──────────────────────────────────────────

    @Test fun `259 minus 449 equals negative 190`() = assertSuccess("259-449", "-190")

    @Test fun `0_1 plus 0_2 equals 0_3`() = assertSuccess("0.1+0.2", "0.3")

    @Test fun `200 plus 10 percent equals 220`() = assertSuccess("200+10%", "220")

    @Test fun `200 minus 10 percent equals 180`() = assertSuccess("200-10%", "180")

    @Test fun `200 times 10 percent equals 20`() = assertSuccess("200*10%", "20")

    @Test fun `50 percent equals 0_5`() = assertSuccess("50%", "0.5")

    @Test fun `200 div 50 percent equals 400`() = assertSuccess("200/50%", "400")

    @Test fun `2 plus 3 times 4 equals 14`() = assertSuccess("2+3*4", "14")

    @Test fun `parentheses 2 plus 3 times 4 equals 20`() = assertSuccess("(2+3)*4", "20")

    @Test fun `2 power 3 power 2 equals 512 right assoc`() = assertSuccess("2^3^2", "512")

    @Test fun `negative 2 power 2 equals negative 4`() = assertSuccess("-2^2", "-4")

    @Test fun `5 div 0 is DivisionByZero`() = assertError("5/0", EvalError.DivisionByZero)

    @Test fun `sqrt negative 1 is DomainError`() = assertError("sqrt(-1)", EvalError.DomainError)

    @Test fun `10 factorial equals 3628800`() = assertSuccess("10!", "3628800")

    @Test fun `sin 30 deg equals 0_5`() = assertSuccess("sin(30)", "0.5", AngleMode.DEG)

    @Test fun `cos 90 deg equals 0`() = assertSuccess("cos(90)", "0", AngleMode.DEG, 1e-10)

    @Test fun `ln e equals 1`() = assertSuccess("ln(e)", "1", AngleMode.DEG, 1e-9)

    @Test fun `log 1000 equals 3`() = assertSuccess("log(1000)", "3", AngleMode.DEG, 1e-9)

    @Test fun `1 div 3 equals 0_333`() {
        val result = eval("1/3")
        assertTrue(result is EvalResult.Success)
        val v = (result as EvalResult.Success).value.toDouble()
        assertEquals(0.3333333333, v, 1e-9)
    }

    @Test fun `implicit multiply 2 paren 3 equals 6`() = assertSuccess("2(3)", "6")

    @Test fun `auto close open paren preview`() {
        // (2+3  should auto-close to (2+3) = 5
        val result = Calculator.evaluate("(2+3")
        assertTrue(result is EvalResult.Success)
        assertEquals(5.0, (result as EvalResult.Success).value.toDouble(), 1e-9)
    }

    // ── Additional tests ───────────────────────────────────────────────────────

    @Test fun `unary minus negative number`() = assertSuccess("-5", "-5")

    @Test fun `nested parentheses`() = assertSuccess("((2+3)*2)", "10")

    @Test fun `pi constant`() {
        val result = eval("pi")
        assertTrue(result is EvalResult.Success)
        assertEquals(Math.PI, (result as EvalResult.Success).value.toDouble(), 1e-10)
    }

    @Test fun `e constant`() {
        val result = eval("e")
        assertTrue(result is EvalResult.Success)
        assertEquals(Math.E, (result as EvalResult.Success).value.toDouble(), 1e-10)
    }

    @Test fun `sin in RAD mode`() = assertSuccess("sin(pi)", "0", AngleMode.RAD, 1e-10)

    @Test fun `empty expression returns zero`() {
        val result = eval("")
        assertTrue(result is EvalResult.Success)
        assertEquals(0.0, (result as EvalResult.Success).value.toDouble(), 0.0)
    }

    @Test fun `asin 0_5 in DEG equals 30`() = assertSuccess("asin(0.5)", "30", AngleMode.DEG, 1e-9)

    @Test fun `acos 1 in DEG equals 0`() = assertSuccess("acos(1)", "0", AngleMode.DEG, 1e-9)

    @Test fun `atan 1 in DEG equals 45`() = assertSuccess("atan(1)", "45", AngleMode.DEG, 1e-9)

    @Test fun `sqrt 4 equals 2`() = assertSuccess("sqrt(4)", "2")

    @Test fun `1000 factorial does not crash`() {
        val result = eval("1000!")
        assertTrue(result is EvalResult.Success)
    }

    @Test fun `asin out of range is DomainError`() = assertError("asin(2)", EvalError.DomainError)

    @Test fun `ln of zero is DomainError`() = assertError("ln(0)", EvalError.DomainError)

    @Test fun `ln of negative is DomainError`() = assertError("ln(-1)", EvalError.DomainError)

    @Test fun `autoClose adds parens`() {
        assertEquals("(2+3)", Calculator.autoClose("(2+3"))
        assertEquals("((2+3))", Calculator.autoClose("((2+3)"))
        assertEquals("2+3", Calculator.autoClose("2+3"))
    }
}
