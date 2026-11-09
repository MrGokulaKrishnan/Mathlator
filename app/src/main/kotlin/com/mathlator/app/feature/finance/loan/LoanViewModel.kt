package com.mathlator.app.feature.finance.loan

import androidx.lifecycle.ViewModel
import com.mathlator.app.core.finance.EmiCalculator
import com.mathlator.app.core.finance.EmiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject

enum class TenureUnit { YEARS, MONTHS }

data class LoanUiState(
    val amountInput: String = "1000000",
    val interestRateInput: String = "9.0",
    val tenureInput: String = "20",
    val tenureUnit: TenureUnit = TenureUnit.YEARS,
    val result: EmiResult = EmiResult(
        emi = BigDecimal("8997.00"),
        totalInterest = BigDecimal("1159342.00"),
        totalPayment = BigDecimal("2159342.00"),
    ),
    val principalRatio: Float = 0.463f,
    val interestRatio: Float = 0.537f,
    val totalMonths: Int = 240,
)

@HiltViewModel
class LoanViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    fun setAmount(v: String) {
        _uiState.update { it.copy(amountInput = v) }
        calculate()
    }

    fun setInterestRate(v: String) {
        _uiState.update { it.copy(interestRateInput = v) }
        calculate()
    }

    fun setTenure(v: String) {
        _uiState.update { it.copy(tenureInput = v) }
        calculate()
    }

    fun setTenureUnit(unit: TenureUnit) {
        _uiState.update { it.copy(tenureUnit = unit) }
        calculate()
    }

    private fun calculate() {
        val s = _uiState.value
        val principal = s.amountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = s.interestRateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tenureRaw = s.tenureInput.toIntOrNull() ?: 1

        val months = if (s.tenureUnit == TenureUnit.YEARS) tenureRaw * 12 else tenureRaw
        if (principal <= BigDecimal.ZERO || months <= 0) return

        val res = EmiCalculator.calculate(
            principal = principal,
            annualRatePct = rate,
            tenureMonths = months,
        )

        val total = res.totalPayment.toFloat().coerceAtLeast(1f)
        val pRatio = (principal.toFloat() / total).coerceIn(0f, 1f)
        val iRatio = (res.totalInterest.toFloat() / total).coerceIn(0f, 1f)

        _uiState.update {
            it.copy(
                result = res,
                principalRatio = pRatio,
                interestRatio = iRatio,
                totalMonths = months,
            )
        }
    }
}
