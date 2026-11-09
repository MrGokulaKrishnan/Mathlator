package com.mathlator.app.core.engine

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Evaluates a tokenized calculator expression using recursive descent parsing.
 *
 * Grammar:
 * ```
 * expr      → additive ( '%' context )?
 * additive  → multiplicative ( ('+' | '-') multiplicative )*
 * multiplicative → power ( ('*' | '/') power )*
 * power     → unary ( '^' power )?           ← right-associative
 * unary     → '-' unary | postfix
 * postfix   → primary '!'*
 * primary   → number | constant | func '(' expr ')' | '(' expr ')'
 * ```
 *
 * Percent is handled contextually at the additive level following Android calculator conventions.
 */
class Evaluator(private val angleMode: AngleMode) {

    companion object {
        private val MC = MathContext.DECIMAL128
        private val ZERO = BigDecimal.ZERO
        private val ONE = BigDecimal.ONE
        private val PI = BigDecimal("3.14159265358979323846264338327950288")
        private val E_CONST = BigDecimal("2.71828182845904523536028747135266250")
        private const val SNAP_THRESHOLD = 1e-12

        /** Maximum factorial argument. */
        private const val MAX_FACTORIAL = 1000L
    }

    private var tokens: List<Token> = emptyList()
    private var pos: Int = 0

    /**
     * Evaluates [tokens] and returns [EvalResult].
     * This function is **not** thread-safe; create a new [Evaluator] per call.
     */
    fun eval(tokenList: List<Token>): EvalResult {
        this.tokens = tokenList
        this.pos = 0
        return try {
            if (tokens.isEmpty()) EvalResult.Success(ZERO)
            else {
                val value = parseExpr(null)
                if (pos < tokens.size) EvalResult.Error(EvalError.Syntax)
                else EvalResult.Success(snapNearZero(value))
            }
        } catch (e: EvalException) {
            EvalResult.Error(e.error)
        } catch (e: Exception) {
            EvalResult.Error(EvalError.Syntax)
        }
    }

    // ── Grammar rules ─────────────────────────────────────────────────────────

    /**
     * Parse top-level expression, applying percent context if needed.
     * [leftContext] is passed when we know the left side of a binary operation
     * (for percent rules like a + b%).
     */
    private fun parseExpr(leftContext: BigDecimal?): BigDecimal = parseAdditive()

    private fun parseAdditive(): BigDecimal {
        var left = parseMultiplicative()

        while (pos < tokens.size) {
            val tok = tokens[pos]
            if (tok !is Token.Op || (tok.symbol != '+' && tok.symbol != '-')) break
            pos++

            // Check for percent on the right side
            val rightRaw = parseMultiplicative()
            val isPercent = pos < tokens.size && tokens[pos] is Token.Op && (tokens[pos] as Token.Op).symbol == '%'
            if (isPercent) {
                pos++
                // Android percent rules:
                // a + b% → a + a×b/100
                // a - b% → a - a×b/100
                val percentVal = left.multiply(rightRaw, MC).divide(BD100, MC)
                left = if (tok.symbol == '+') left.add(percentVal, MC) else left.subtract(percentVal, MC)
            } else {
                left = if (tok.symbol == '+') left.add(rightRaw, MC) else left.subtract(rightRaw, MC)
            }
        }

        // Handle standalone % or multiplicative percent
        if (pos < tokens.size && tokens[pos] is Token.Op && (tokens[pos] as Token.Op).symbol == '%') {
            pos++
            left = left.divide(BD100, MC)
        }

        return left
    }

    private fun parseMultiplicative(): BigDecimal {
        var left = parseUnary()

        while (pos < tokens.size) {
            val tok = tokens[pos]
            if (tok !is Token.Op || (tok.symbol != '*' && tok.symbol != '/')) break
            pos++
            val right = parseUnary()
            left = when (tok.symbol) {
                '*' -> left.multiply(right, MC)
                '/' -> {
                    // Check for percent on right: a × b% → a × b/100; a ÷ b% → a ÷ (b/100)
                    val isPercent = pos < tokens.size && tokens[pos] is Token.Op && (tokens[pos] as Token.Op).symbol == '%'
                    if (isPercent) {
                        pos++
                        if (tok.symbol == '*') {
                            // already handled above
                        }
                        // a ÷ b% → a ÷ (b/100) = a * 100 / b
                        left.multiply(BD100, MC).divide(right, MC)
                    } else {
                        if (right.compareTo(ZERO) == 0) throw EvalException(EvalError.DivisionByZero)
                        left.divide(right, MC)
                    }
                }
                else -> throw EvalException(EvalError.Syntax)
            }
            // Handle multiply-percent outside of above: a * b%
            if (tok.symbol == '*') {
                // peek for %
                if (pos < tokens.size && tokens[pos] is Token.Op && (tokens[pos] as Token.Op).symbol == '%') {
                    pos++
                    left = left.divide(BD100, MC) // a * b already computed; now /100
                }
            }
        }
        return left
    }

    private fun parseUnary(): BigDecimal {
        if (pos < tokens.size && tokens[pos] == Token.UnaryMinus) {
            pos++
            return parseUnary().negate()
        }
        return parsePower()
    }

    // Right-associative power (binds tighter than unary minus)
    private fun parsePower(): BigDecimal {
        val base = parsePostfix()
        if (pos < tokens.size && tokens[pos] is Token.Op && (tokens[pos] as Token.Op).symbol == '^') {
            pos++
            val exp = parsePower() // right-associative recursive call
            return power(base, exp)
        }
        return base
    }

    private fun parsePostfix(): BigDecimal {
        var value = parsePrimary()
        while (pos < tokens.size && tokens[pos] == Token.Factorial) {
            pos++
            value = factorial(value)
        }
        return value
    }

    private fun parsePrimary(): BigDecimal {
        if (pos >= tokens.size) throw EvalException(EvalError.Syntax)

        return when (val tok = tokens[pos]) {
            is Token.Num -> {
                pos++
                tok.value.toBigDecimalOrNull() ?: throw EvalException(EvalError.Syntax)
            }

            is Token.Const -> {
                pos++
                when (tok.name) {
                    "pi" -> PI
                    "e"  -> E_CONST
                    else -> throw EvalException(EvalError.Syntax)
                }
            }

            is Token.Func -> {
                pos++
                // Expect '('
                if (pos >= tokens.size || tokens[pos] != Token.LParen) throw EvalException(EvalError.Syntax)
                pos++ // consume '('
                val arg = parseExpr(null)
                if (pos >= tokens.size || tokens[pos] != Token.RParen) throw EvalException(EvalError.Syntax)
                pos++ // consume ')'
                applyFunction(tok.name, arg)
            }

            Token.LParen -> {
                pos++
                val inner = parseExpr(null)
                if (pos >= tokens.size || tokens[pos] != Token.RParen) throw EvalException(EvalError.Syntax)
                pos++
                inner
            }

            else -> throw EvalException(EvalError.Syntax)
        }
    }

    // ── Math helpers ──────────────────────────────────────────────────────────

    private fun applyFunction(name: String, arg: BigDecimal): BigDecimal {
        return when (name) {
            "sin"  -> snapD(sin(toRad(arg.toDouble())))
            "cos"  -> snapD(cos(toRad(arg.toDouble())))
            "tan"  -> {
                val rad = toRad(arg.toDouble())
                // Check for undefined (90°, 270° etc.)
                val cosVal = cos(rad)
                if (abs(cosVal) < SNAP_THRESHOLD) throw EvalException(EvalError.DomainError)
                snapD(tan(rad))
            }
            "asin" -> {
                val d = arg.toDouble()
                if (d < -1.0 || d > 1.0) throw EvalException(EvalError.DomainError)
                fromRad(asin(d))
            }
            "acos" -> {
                val d = arg.toDouble()
                if (d < -1.0 || d > 1.0) throw EvalException(EvalError.DomainError)
                fromRad(acos(d))
            }
            "atan" -> fromRad(atan(arg.toDouble()))
            "ln"   -> {
                if (arg.compareTo(ZERO) <= 0) throw EvalException(EvalError.DomainError)
                snapD(ln(arg.toDouble()))
            }
            "log"  -> {
                if (arg.compareTo(ZERO) <= 0) throw EvalException(EvalError.DomainError)
                snapD(log10(arg.toDouble()))
            }
            "sqrt" -> {
                if (arg.compareTo(ZERO) < 0) throw EvalException(EvalError.DomainError)
                arg.sqrt(MC)
            }
            "exp"  -> snapD(exp(arg.toDouble()))
            "pow10" -> snapD(10.0.pow(arg.toDouble()))
            else   -> throw EvalException(EvalError.Syntax)
        }
    }

    private fun toRad(deg: Double): Double =
        if (angleMode == AngleMode.DEG) Math.toRadians(deg) else deg

    private fun fromRad(rad: Double): BigDecimal =
        if (angleMode == AngleMode.DEG) BigDecimal(Math.toDegrees(rad), MC) else BigDecimal(rad, MC)

    private fun snapD(d: Double): BigDecimal {
        if (!d.isFinite()) throw EvalException(EvalError.Overflow)
        val snapped = if (abs(d) < SNAP_THRESHOLD) 0.0 else d
        return BigDecimal(snapped, MC)
    }

    private fun power(base: BigDecimal, exp: BigDecimal): BigDecimal {
        // Try integer exponent first for exactness
        return try {
            val expInt = exp.stripTrailingZeros().scale()
            if (exp.scale() <= 0 || expInt == 0) {
                val expLong = exp.toLongExact()
                if (expLong >= 0) base.pow(expLong.toInt(), MC)
                else ONE.divide(base.pow((-expLong).toInt(), MC), MC)
            } else {
                snapD(base.toDouble().pow(exp.toDouble()))
            }
        } catch (e: ArithmeticException) {
            snapD(base.toDouble().pow(exp.toDouble()))
        }
    }

    private fun factorial(n: BigDecimal): BigDecimal {
        if (n.scale() > 0 && n.stripTrailingZeros().scale() > 0) throw EvalException(EvalError.DomainError)
        val long = try { n.toLongExact() } catch (e: ArithmeticException) { throw EvalException(EvalError.DomainError) }
        if (long < 0 || long > MAX_FACTORIAL) throw EvalException(EvalError.DomainError)
        var result = BigInteger.ONE
        for (i in 2..long) result = result.multiply(BigInteger.valueOf(i))
        return BigDecimal(result, MC)
    }

    private fun snapNearZero(v: BigDecimal): BigDecimal {
        return if (v.abs().compareTo(BigDecimal("1e-12")) < 0 && v.compareTo(ZERO) != 0) ZERO
        else v
    }

    private val BD100 = BigDecimal("100")
}

/** Internal exception carrying an [EvalError]. */
internal class EvalException(val error: EvalError) : RuntimeException(error.name)

// Extension helpers
private fun BigDecimal.toLongExact(): Long = this.toLong().also {
    if (this.compareTo(BigDecimal(it)) != 0) throw ArithmeticException("Not exact long")
}
