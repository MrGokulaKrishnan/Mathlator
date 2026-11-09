package com.mathlator.app.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.core.engine.Calculator
import com.mathlator.app.core.engine.EvalError
import com.mathlator.app.core.engine.EvalResult
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.data.db.HistoryDao
import com.mathlator.app.data.db.HistoryEntity
import com.mathlator.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val historyDao: HistoryDao,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private var justCalculated = false
    private var lastValidPreview = ""

    init {
        // Collect user preferences
        viewModelScope.launch {
            settings.angleMode.collect { mode ->
                _state.update { it.copy(angleMode = mode) }
                updateLivePreview(_state.value.expression)
            }
        }
        viewModelScope.launch {
            settings.decimalPrecision.collect { prec ->
                _state.update { it.copy(precision = prec) }
                updateLivePreview(_state.value.expression)
            }
        }
        viewModelScope.launch {
            settings.digitGrouping.collect { grp ->
                _state.update { it.copy(grouping = grp) }
                updateDisplayExpression(_state.value.expression)
            }
        }
    }

    fun onKey(key: String) {
        val currentExpr = _state.value.expression

        when (key) {
            "C" -> clearAll()
            "⌫" -> backspace()
            "=" -> evaluateAndCommit()
            "2nd" -> _state.update { it.copy(isSecondFunction = !it.isSecondFunction) }
            "DEG_RAD" -> toggleAngleMode()
            "+", "−", "-", "×", "*", "÷", "/", "^", "%" -> handleOperator(key)
            "." -> handleDecimal()
            "x²" -> handleSquare()
            "√" -> appendFunction("sqrt(")
            "sin", "cos", "tan", "asin", "acos", "atan", "ln", "log", "exp", "pow10" -> appendFunction("$key(")
            "!" -> appendFactorial()
            "π" -> appendConstant("π")
            "e" -> appendConstant("e")
            "(", ")" -> handleParen(key)
            else -> handleDigit(key)
        }
    }

    fun setScientificMode(enabled: Boolean) {
        _state.update { it.copy(isScientific = enabled) }
    }

    fun toggleScientificMode() {
        _state.update { it.copy(isScientific = !it.isScientific) }
    }

    fun loadExpression(expr: String) {
        _state.update {
            it.copy(
                expression = expr,
                isError = false,
                errorMessage = ""
            )
        }
        justCalculated = false
        updateDisplayExpression(expr)
        updateLivePreview(expr)
    }

    private fun clearAll() {
        _state.update {
            it.copy(
                expression = "",
                displayExpression = "",
                result = "",
                isError = false,
                errorMessage = ""
            )
        }
        justCalculated = false
        lastValidPreview = ""
    }

    fun backspace() {
        val expr = _state.value.expression
        if (expr.isEmpty()) return

        val newExpr = when {
            expr.endsWith("sqrt(") -> expr.dropLast(5)
            expr.endsWith("asin(") || expr.endsWith("acos(") || expr.endsWith("atan(") -> expr.dropLast(5)
            expr.endsWith("sin(") || expr.endsWith("cos(") || expr.endsWith("tan(") ||
            expr.endsWith("log(") || expr.endsWith("exp(") -> expr.dropLast(4)
            expr.endsWith("ln(") -> expr.dropLast(3)
            expr.endsWith("pow10(") -> expr.dropLast(6)
            else -> expr.dropLast(1)
        }

        _state.update {
            it.copy(
                expression = newExpr,
                isError = false,
                errorMessage = ""
            )
        }
        justCalculated = false
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun handleDigit(digit: String) {
        if (justCalculated) {
            // New expression starts
            _state.update { it.copy(expression = digit, isError = false, errorMessage = "") }
            justCalculated = false
            updateDisplayExpression(digit)
            updateLivePreview(digit)
            return
        }

        val expr = _state.value.expression
        val lastNumber = extractLastNumber(expr)

        // Rule: Max 15 digits per number
        if (lastNumber.replace(".", "").length >= 15) return

        // Rule: Collapse leading zeros
        val newExpr = if (lastNumber == "0" && digit != ".") {
            expr.dropLast(1) + digit
        } else {
            expr + digit
        }

        _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun handleDecimal() {
        if (justCalculated) {
            _state.update { it.copy(expression = "0.", isError = false, errorMessage = "") }
            justCalculated = false
            updateDisplayExpression("0.")
            updateLivePreview("0.")
            return
        }

        val expr = _state.value.expression
        val lastNumber = extractLastNumber(expr)

        // Rule: One decimal point per number
        if (lastNumber.contains(".")) return

        val newExpr = when {
            expr.isEmpty() || isOperatorOrParen(expr.last()) -> expr + "0."
            else -> expr + "."
        }

        _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun handleOperator(op: String) {
        val standardOp = when (op) {
            "-" -> "−"
            "*" -> "×"
            "/" -> "÷"
            else -> op
        }

        var expr = _state.value.expression

        if (justCalculated) {
            // Continue from the answer
            val prevResult = _state.value.result
            if (prevResult.isNotEmpty() && !_state.value.isError) {
                expr = prevResult
            }
            justCalculated = false
        }

        if (expr.isEmpty()) {
            if (standardOp == "−") {
                expr = "−"
            } else {
                return
            }
        } else {
            val lastChar = expr.last()

            // Allow unary minus after operator or '('
            if (standardOp == "−" && (lastChar == '+' || lastChar == '×' || lastChar == '÷' || lastChar == '(')) {
                expr += "−"
            } else if (isOperator(lastChar)) {
                // Rule: Consecutive operator replaces previous one
                expr = expr.dropLast(1) + standardOp
            } else {
                expr += standardOp
            }
        }

        _state.update { it.copy(expression = expr, isError = false, errorMessage = "") }
        updateDisplayExpression(expr)
        updateLivePreview(expr)
    }

    private fun handleParen(p: String) {
        val expr = _state.value.expression
        val newExpr = if (p == "(") {
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
                expr + "×(" // Auto-insert ×
            } else {
                expr + "("
            }
        } else {
            expr + ")"
        }

        _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
        justCalculated = false
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun appendFunction(fn: String) {
        val expr = _state.value.expression
        val newExpr = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
            expr + "×" + fn
        } else {
            expr + fn
        }
        _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
        justCalculated = false
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun appendConstant(const: String) {
        val expr = _state.value.expression
        val newExpr = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
            expr + "×" + const
        } else {
            expr + const
        }
        _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
        justCalculated = false
        updateDisplayExpression(newExpr)
        updateLivePreview(newExpr)
    }

    private fun appendFactorial() {
        val expr = _state.value.expression
        if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
            val newExpr = expr + "!"
            _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
            justCalculated = false
            updateDisplayExpression(newExpr)
            updateLivePreview(newExpr)
        }
    }

    private fun handleSquare() {
        val expr = _state.value.expression
        if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
            val newExpr = expr + "^2"
            _state.update { it.copy(expression = newExpr, isError = false, errorMessage = "") }
            justCalculated = false
            updateDisplayExpression(newExpr)
            updateLivePreview(newExpr)
        }
    }

    private fun toggleAngleMode() {
        val newMode = if (_state.value.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        viewModelScope.launch {
            settings.setAngleMode(newMode)
        }
    }

    private fun evaluateAndCommit() {
        val expr = _state.value.expression
        if (expr.isBlank()) return

        val evalResult = Calculator.evaluate(expr, _state.value.angleMode)
        when (evalResult) {
            is EvalResult.Success -> {
                val formatted = NumberFormatter.format(
                    value = evalResult.value,
                    precision = _state.value.precision,
                    grouping = _state.value.grouping,
                )
                _state.update {
                    it.copy(
                        result = formatted,
                        isError = false,
                        errorMessage = "",
                    )
                }
                justCalculated = true
                lastValidPreview = formatted

                // Save to Room DB history
                viewModelScope.launch {
                    historyDao.insert(
                        HistoryEntity(
                            expression = expr,
                            result = formatted,
                        )
                    )
                    historyDao.pruneOldest(500)
                }
            }
            is EvalResult.Error -> {
                val errText = when (evalResult.type) {
                    EvalError.DivisionByZero -> "Can't divide by 0"
                    EvalError.DomainError -> "Domain error"
                    EvalError.Overflow -> "Number too large"
                    EvalError.Syntax -> "Invalid expression"
                }
                _state.update {
                    it.copy(
                        isError = true,
                        errorMessage = errText,
                    )
                }
            }
        }
    }

    private fun updateLivePreview(expr: String) {
        if (expr.isBlank()) {
            _state.update { it.copy(result = "") }
            return
        }

        val evalResult = Calculator.evaluate(expr, _state.value.angleMode)
        if (evalResult is EvalResult.Success) {
            val formatted = NumberFormatter.format(
                value = evalResult.value,
                precision = _state.value.precision,
                grouping = _state.value.grouping,
            )
            lastValidPreview = formatted
            _state.update { it.copy(result = formatted, isError = false) }
        } else {
            // Keep last valid preview if syntax incomplete
            if (lastValidPreview.isNotEmpty()) {
                _state.update { it.copy(result = lastValidPreview, isError = false) }
            }
        }
    }

    private fun updateDisplayExpression(expr: String) {
        _state.update { it.copy(displayExpression = expr) }
    }

    private fun extractLastNumber(expr: String): String {
        var i = expr.length - 1
        while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) {
            i--
        }
        return expr.substring(i + 1)
    }

    private fun isOperator(c: Char): Boolean =
        c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^' || c == '%'

    private fun isOperatorOrParen(c: Char): Boolean =
        isOperator(c) || c == '(' || c == ')'
}
