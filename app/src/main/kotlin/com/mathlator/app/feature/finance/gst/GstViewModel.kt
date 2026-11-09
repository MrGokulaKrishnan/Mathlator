package com.mathlator.app.feature.finance.gst

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mathlator.app.core.finance.GstCalculator
import com.mathlator.app.core.finance.GstResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject

data class GstUiState(
    val isAddGst: Boolean = true,
    val amountInput: String = "1000",
    val availableRates: List<Int> = listOf(0, 3, 5, 18, 40),
    val selectedRate: Int = 18,
    val isCustomRate: Boolean = false,
    val customRateInput: String = "12",
    val isIgst: Boolean = false,
    val result: GstResult = GstResult(
        baseAmount = BigDecimal("1000.00"),
        gstAmount = BigDecimal("180.00"),
        totalAmount = BigDecimal("1180.00"),
        cgst = BigDecimal("90.00"),
        sgst = BigDecimal("90.00"),
    ),
)

@HiltViewModel
class GstViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GstUiState())
    val uiState: StateFlow<GstUiState> = _uiState.asStateFlow()

    init {
        loadRatesFromAssets()
        calculate()
    }

    private fun loadRatesFromAssets() {
        try {
            val jsonStr = context.assets.open("gst_rates.json").bufferedReader().use { it.readText() }
            val rates = Json.decodeFromString<List<Int>>(jsonStr)
            _uiState.update { it.copy(availableRates = rates) }
        } catch (_: Exception) {
            // Keep default fallback
        }
    }

    fun setIsAddGst(isAdd: Boolean) {
        _uiState.update { it.copy(isAddGst = isAdd) }
        calculate()
    }

    fun setAmountInput(v: String) {
        _uiState.update { it.copy(amountInput = v) }
        calculate()
    }

    fun selectRate(rate: Int) {
        _uiState.update { it.copy(selectedRate = rate, isCustomRate = false) }
        calculate()
    }

    fun selectCustomRate() {
        _uiState.update { it.copy(isCustomRate = true) }
        calculate()
    }

    fun setCustomRateInput(v: String) {
        _uiState.update { it.copy(customRateInput = v) }
        calculate()
    }

    fun setIgst(isIgst: Boolean) {
        _uiState.update { it.copy(isIgst = isIgst) }
    }

    private fun calculate() {
        val s = _uiState.value
        val amount = s.amountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = if (s.isCustomRate) {
            s.customRateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        } else {
            BigDecimal(s.selectedRate)
        }

        if (amount <= BigDecimal.ZERO) return

        val res = if (s.isAddGst) {
            GstCalculator.addGst(amount, rate)
        } else {
            GstCalculator.removeGst(amount, rate)
        }

        _uiState.update { it.copy(result = res) }
    }
}
