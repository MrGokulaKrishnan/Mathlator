package com.mathlator.app.core.engine

import java.math.BigDecimal

/** Result of a calculation attempt. */
sealed class EvalResult {
    /** Successfully evaluated; [value] is the exact result. */
    data class Success(val value: BigDecimal) : EvalResult()

    /** An error occurred during evaluation. */
    data class Error(val type: EvalError) : EvalResult()
}

/** Categories of evaluation errors. */
enum class EvalError {
    DivisionByZero,
    DomainError,     // √ of negative, ln ≤ 0, asin/acos out of range
    Overflow,
    Syntax,
}
