package com.mathlator.app.feature.calculator

import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.core.format.DigitGrouping

data class CalculatorState(
    val expression: String = "",
    val displayExpression: String = "", // formatted for display (digit grouping in numeric segments)
    val result: String = "",
    val isError: Boolean = false,
    val errorMessage: String = "",
    val angleMode: AngleMode = AngleMode.DEG,
    val isScientific: Boolean = false,
    val isSecondFunction: Boolean = false, // 2nd key toggle
    val precision: Int = 10,
    val grouping: DigitGrouping = DigitGrouping.Indian,
)
