package com.mathlator.app.core.engine

/** Tokens produced by the [Tokenizer]. */
sealed class Token {
    data class Num(val value: String) : Token()
    data class Op(val symbol: Char) : Token()       // + - * / ^ %
    data class Func(val name: String) : Token()     // sin, cos, sqrt, etc.
    data class Const(val name: String) : Token()    // pi, e
    object LParen : Token()
    object RParen : Token()
    object UnaryMinus : Token()
    object Factorial : Token()
}
