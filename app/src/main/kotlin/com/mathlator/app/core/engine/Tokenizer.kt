package com.mathlator.app.core.engine

/**
 * Tokenizes a calculator expression string into a list of [Token]s.
 *
 * Accepted characters/sequences:
 * - Digits and `.` for numbers.
 * - Operators: `+`, `-`, `*`, `×`, `/`, `÷`, `^`, `%`.
 * - Functions: sin, cos, tan, asin, acos, atan, ln, log, sqrt, √.
 * - Constants: π, pi, e.
 * - Parentheses: `(`, `)`.
 * - Factorial: `!`.
 * - Implicit multiplication: `2(3)`, `2sin(…)`.
 */
object Tokenizer {

    private val FUNCTIONS = listOf(
        "asin", "acos", "atan", "sin", "cos", "tan", "ln", "log", "sqrt",
    )

    /**
     * Tokenizes [expression] and returns the list of tokens.
     *
     * @throws IllegalArgumentException for unrecognized characters.
     */
    fun tokenize(expression: String): List<Token> {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt(")  // √x → sqrt(x) — handled by auto-paren logic elsewhere
            .replace("π", "pi")
            .trim()

        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < cleaned.length) {
            val ch = cleaned[i]

            when {
                ch.isWhitespace() -> { i++; continue }

                // Number (integer or decimal)
                ch.isDigit() || ch == '.' -> {
                    val sb = StringBuilder()
                    while (i < cleaned.length && (cleaned[i].isDigit() || cleaned[i] == '.')) {
                        sb.append(cleaned[i++])
                    }
                    tokens.add(Token.Num(sb.toString()))
                }

                // Functions and constants
                ch.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < cleaned.length && cleaned[i].isLetter()) {
                        sb.append(cleaned[i++])
                    }
                    val word = sb.toString().lowercase()
                    when {
                        word in FUNCTIONS -> tokens.add(Token.Func(word))
                        word == "pi"      -> tokens.add(Token.Const("pi"))
                        word == "e"       -> tokens.add(Token.Const("e"))
                        else              -> tokens.add(Token.Func(word)) // unknown → will error at eval
                    }
                }

                // π as single char (already replaced, but guard)
                ch == 'π' -> { tokens.add(Token.Const("pi")); i++ }

                // Operators
                ch == '+' -> { tokens.add(Token.Op('+')); i++ }
                ch == '-' -> { tokens.add(Token.Op('-')); i++ }
                ch == '*' -> { tokens.add(Token.Op('*')); i++ }
                ch == '/' -> { tokens.add(Token.Op('/')); i++ }
                ch == '^' -> { tokens.add(Token.Op('^')); i++ }
                ch == '%' -> { tokens.add(Token.Op('%')); i++ }

                ch == '(' -> { tokens.add(Token.LParen); i++ }
                ch == ')' -> { tokens.add(Token.RParen); i++ }
                ch == '!' -> { tokens.add(Token.Factorial); i++ }

                else -> throw IllegalArgumentException("Unexpected character: '$ch'")
            }
        }

        return resolveUnaryAndImplicit(tokens)
    }

    /**
     * Post-processes the raw token list to:
     * 1. Resolve unary minus (−) where `-` follows an operator, `(`, or start.
     * 2. Insert implicit `×` before `(` or a function when preceded by a number, constant, `)`, or `!`.
     */
    private fun resolveUnaryAndImplicit(raw: List<Token>): List<Token> {
        val result = mutableListOf<Token>()

        for ((idx, tok) in raw.withIndex()) {
            val prev = result.lastOrNull()

            // Resolve unary minus
            if (tok is Token.Op && tok.symbol == '-') {
                val isUnary = prev == null
                    || prev is Token.Op
                    || prev is Token.Func
                    || prev == Token.LParen
                    || prev == Token.UnaryMinus
                result.add(if (isUnary) Token.UnaryMinus else tok)
                continue
            }

            // Implicit multiplication before ( or function
            if (tok == Token.LParen || tok is Token.Func || tok is Token.Const) {
                if (prev is Token.Num || prev is Token.Const || prev == Token.RParen || prev == Token.Factorial) {
                    result.add(Token.Op('*'))
                }
            }

            result.add(tok)
        }

        return result
    }
}
