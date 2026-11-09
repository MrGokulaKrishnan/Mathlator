package com.mathlator.app.feature.converters.discount

import androidx.lifecycle.ViewModel
import com.mathlator.app.core.finance.DiscountCalculator
import com.mathlator.app.core.finance.DiscountResult
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.core.format.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject

data class DiscountUiState(
    val priceInput: String = "1000",
    val discountPct: String = "20",
    val extraDiscountPct: String = "10",
    val taxPct: String = "0",
    val isReverse: Boolean = false,
    val result: DiscountResult = DiscountResult(
        finalPrice = BigDecimal("720.00"),
        youSave = BigDecimal("280.00"),
        effectiveDiscount = BigDecimal("28.00"),
    ),
)

@HiltViewModel
class DiscountViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DiscountUiState())
    val uiState: StateFlow<DiscountUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    fun setPriceInput(v: String) {
        _uiState.update { it.copy(priceInput = v) }
        calculate()
    }

    fun setDiscountPct(v: String) {
        _uiState.update { it.copy(discountPct = v) }
        calculate()
    }

    fun setExtraDiscountPct(v: String) {
        _uiState.update { it.copy(extraDiscountPct = v) }
        calculate()
    }

    fun setTaxPct(v: String) {
        _uiState.update { it.copy(taxPct = v) }
        calculate()
    }

    fun setReverse(isReverse: Boolean) {
        _uiState.update { it.copy(isReverse = isReverse) }
        calculate()
    }

    private fun calculate() {
        val s = _uiState.value
        val price = s.priceInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val disc = s.discountPct.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val extra = s.extraDiscountPct.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tax = s.taxPct.toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (price <= BigDecimal.ZERO) return

        val res = if (s.isReverse) {
            DiscountCalculator.reverse(
                finalPrice = price,
                discountPct = disc,
                taxPct = tax,
            )
        } else {
            DiscountCalculator.calculate(
                originalPrice = price,
                discountPct = disc,
                extraDiscountPct = extra,
                taxPct = tax,
            )
        }

        _uiState.update { it.copy(result = res) }
    }
}
