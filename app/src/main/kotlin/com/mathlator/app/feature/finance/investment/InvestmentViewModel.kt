package com.mathlator.app.feature.finance.investment

import androidx.lifecycle.ViewModel
import com.mathlator.app.core.finance.CompoundingFrequency
import com.mathlator.app.core.finance.FdCalculator
import com.mathlator.app.core.finance.InvestmentResult
import com.mathlator.app.core.finance.LumpSumCalculator
import com.mathlator.app.core.finance.SipCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

enum class InvestmentTab { SIP, LUMPSUM, FD }

data class InvestmentUiState(
    val selectedTab: InvestmentTab = InvestmentTab.SIP,
    // SIP inputs
    val sipMonthlyAmount: String = "5000",
    val sipReturnRatePct: Float = 12f,
    val sipYears: Int = 10,
    val sipStepUpPct: Float = 0f,
    // Lump sum inputs
    val lumpSumPrincipal: String = "100000",
    val lumpSumReturnRatePct: Float = 10f,
    val lumpSumYears: Int = 5,
    // FD inputs
    val fdPrincipal: String = "100000",
    val fdReturnRatePct: Float = 7f,
    val fdYears: Int = 5,
    val fdFrequency: CompoundingFrequency = CompoundingFrequency.Quarterly,
    val fdEffectiveYield: String = "7.19%",
    // Result
    val result: InvestmentResult = InvestmentResult(
        invested = BigDecimal("600000.00"),
        estimatedReturn = BigDecimal("561695.00"),
        totalValue = BigDecimal("1161695.00"),
    ),
    val investedSeries: List<Pair<Int, BigDecimal>> = emptyList(),
    val valueSeries: List<Pair<Int, BigDecimal>> = emptyList(),
)

@HiltViewModel
class InvestmentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState: StateFlow<InvestmentUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    fun selectTab(tab: InvestmentTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        calculate()
    }

    fun setSipMonthlyAmount(v: String) {
        _uiState.update { it.copy(sipMonthlyAmount = v) }
        calculate()
    }

    fun setSipReturnRate(v: Float) {
        _uiState.update { it.copy(sipReturnRatePct = v) }
        calculate()
    }

    fun setSipYears(v: Int) {
        _uiState.update { it.copy(sipYears = v) }
        calculate()
    }

    fun setSipStepUp(v: Float) {
        _uiState.update { it.copy(sipStepUpPct = v) }
        calculate()
    }

    fun setLumpSumPrincipal(v: String) {
        _uiState.update { it.copy(lumpSumPrincipal = v) }
        calculate()
    }

    fun setLumpSumReturnRate(v: Float) {
        _uiState.update { it.copy(lumpSumReturnRatePct = v) }
        calculate()
    }

    fun setLumpSumYears(v: Int) {
        _uiState.update { it.copy(lumpSumYears = v) }
        calculate()
    }

    fun setFdPrincipal(v: String) {
        _uiState.update { it.copy(fdPrincipal = v) }
        calculate()
    }

    fun setFdReturnRate(v: Float) {
        _uiState.update { it.copy(fdReturnRatePct = v) }
        calculate()
    }

    fun setFdYears(v: Int) {
        _uiState.update { it.copy(fdYears = v) }
        calculate()
    }

    fun setFdFrequency(freq: CompoundingFrequency) {
        _uiState.update { it.copy(fdFrequency = freq) }
        calculate()
    }

    private fun calculate() {
        val s = _uiState.value
        when (s.selectedTab) {
            InvestmentTab.SIP -> {
                val monthly = s.sipMonthlyAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val res = SipCalculator.calculate(
                    monthlyAmount = monthly,
                    annualRatePct = BigDecimal(s.sipReturnRatePct.toString()),
                    years = s.sipYears,
                    stepUpPct = BigDecimal(s.sipStepUpPct.toString()),
                )
                val investedPts = (1..s.sipYears).map { y ->
                    y to monthly.multiply(BigDecimal(y * 12))
                }
                _uiState.update {
                    it.copy(
                        result = res,
                        investedSeries = investedPts,
                        valueSeries = res.yearlyValues,
                    )
                }
            }
            InvestmentTab.LUMPSUM -> {
                val principal = s.lumpSumPrincipal.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val res = LumpSumCalculator.calculate(
                    principal = principal,
                    annualRatePct = BigDecimal(s.lumpSumReturnRatePct.toString()),
                    years = s.lumpSumYears,
                )
                val investedPts = (1..s.lumpSumYears).map { y -> y to principal }
                _uiState.update {
                    it.copy(
                        result = res,
                        investedSeries = investedPts,
                        valueSeries = res.yearlyValues,
                    )
                }
            }
            InvestmentTab.FD -> {
                val principal = s.fdPrincipal.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val res = FdCalculator.calculate(
                    principal = principal,
                    annualRatePct = BigDecimal(s.fdReturnRatePct.toString()),
                    years = s.fdYears,
                    frequency = s.fdFrequency,
                )
                val investedPts = (1..s.fdYears).map { y -> y to principal }

                // Effective annual yield = (1 + r/(100n))^n - 1
                val n = BigDecimal(s.fdFrequency.timesPerYear)
                val rOver100n = BigDecimal(s.fdReturnRatePct.toString()).divide(BigDecimal(100).multiply(n), MathContext.DECIMAL128)
                val yield = BigDecimal.ONE.add(rOver100n)
                    .pow(s.fdFrequency.timesPerYear, MathContext.DECIMAL128)
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)

                _uiState.update {
                    it.copy(
                        result = res,
                        investedSeries = investedPts,
                        valueSeries = res.yearlyValues,
                        fdEffectiveYield = "$yield%",
                    )
                }
            }
        }
    }
}
