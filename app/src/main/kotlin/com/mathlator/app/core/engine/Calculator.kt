package com.mathlator.app.core.engine

import java.math.BigDecimal

/**
 * Public API for the calculation engine.
 *
 * Usage:
 * ```kotlin
 * val result = Calculator.evaluate("2+3×4", AngleMode.DEG)
 * ```
 *
 * This object is stateless and thread-safe.
 */
object Calculator {

    /**
     * Evaluates the given [expression] string and returns an [EvalResult].
     *
     * Before evaluation, any auto-close of open parentheses is applied
     * (for live-preview). The expression is tokenized then evaluated.
     *
     * @param expression  The expression string using `+`, `-`, `×` or `*`,
     *                    `÷` or `/`, `^`, `%`, parentheses, and function names.
     * @param angle       Angle mode for trigonometric functions.
     * @return [EvalResult.Success] with the result, or [EvalResult.Error].
     */
    fun evaluate(expression: String, angle: AngleMode = AngleMode.DEG): EvalResult {
        return try {
            val closed = autoClose(expression)
            val tokens = Tokenizer.tokenize(closed)
            Evaluator(angle).eval(tokens)
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(EvalError.Syntax)
        } catch (e: Exception) {
            EvalResult.Error(EvalError.Syntax)
        }
    }

    /**
     * Auto-closes open parentheses in [expression] for preview purposes.
     * E.g., `"(2+3"` → `"(2+3)"`.
     */
    fun autoClose(expression: String): String {
        var depth = 0
        for (ch in expression) {
            when (ch) {
                '(' -> depth++
                ')' -> if (depth > 0) depth--
            }
        }
        return expression + ")".repeat(depth)
    }

    /**
     * Returns true if [expression] is non-empty and syntactically complete
     * (i.e., suitable for saving to history).
     */
    fun isComplete(expression: String): Boolean {
        if (expression.isBlank()) return false
        val result = evaluate(expression)
        return result is EvalResult.Success
    }
}
