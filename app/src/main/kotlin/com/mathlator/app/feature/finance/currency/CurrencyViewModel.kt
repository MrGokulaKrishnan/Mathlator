package com.mathlator.app.feature.finance.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.data.rates.RatesRepository
import com.mathlator.app.data.rates.RatesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CurrencyItem(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val isPinned: Boolean = false,
)

data class CurrencyUiState(
    val fromCode: String = "INR",
    val toCode: String = "USD",
    val fromValue: String = "100",
    val toValue: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val lastUpdatedText: String = "",
    val isNoData: Boolean = false,
    val attributionText: String = "Rates from open.er-api.com",
)

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val ratesRepository: RatesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    private var cachedRates: Map<String, BigDecimal> = emptyMap()

    val currencyList: List<CurrencyItem> = listOf(
        CurrencyItem("INR", "Indian Rupee", "🇮🇳", isPinned = true),
        CurrencyItem("USD", "US Dollar", "🇺🇸", isPinned = true),
        CurrencyItem("EUR", "Euro", "🇪🇺", isPinned = true),
        CurrencyItem("GBP", "British Pound", "🇬🇧", isPinned = true),
        CurrencyItem("JPY", "Japanese Yen", "🇯🇵", isPinned = true),
        CurrencyItem("AUD", "Australian Dollar", "🇦🇺", isPinned = true),
        CurrencyItem("CAD", "Canadian Dollar", "🇨🇦", isPinned = true),
        CurrencyItem("CHF", "Swiss Franc", "🇨🇭", isPinned = true),
        CurrencyItem("CNY", "Chinese Yuan", "🇨🇳", isPinned = true),
        CurrencyItem("SGD", "Singapore Dollar", "🇸🇬", isPinned = true),
        CurrencyItem("AED", "UAE Dirham", "🇦🇪", isPinned = true),
        CurrencyItem("SAR", "Saudi Riyal", "🇸🇦", isPinned = false),
        CurrencyItem("NZD", "New Zealand Dollar", "🇳🇿", isPinned = false),
        CurrencyItem("KRW", "South Korean Won", "🇰🇷", isPinned = false),
        CurrencyItem("BRL", "Brazilian Real", "🇧🇷", isPinned = false),
        CurrencyItem("ZAR", "South African Rand", "🇿🇦", isPinned = false),
        CurrencyItem("MXN", "Mexican Peso", "🇲🇽", isPinned = false),
        CurrencyItem("THB", "Thai Baht", "🇹🇭", isPinned = false),
        CurrencyItem("MYR", "Malaysian Ringgit", "🇲🇾", isPinned = false),
    )

    init {
        // Collect default currency pair
        viewModelScope.launch {
            settings.currencyFrom.collect { code ->
                _uiState.update { it.copy(fromCode = code) }
            }
        }
        viewModelScope.launch {
            settings.currencyTo.collect { code ->
                _uiState.update { it.copy(toCode = code) }
            }
        }
        loadRates(force = false)
    }

    fun loadRates(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isNoData = false) }
            val state = ratesRepository.refresh(base = "USD", force = force)
            when (state) {
                is RatesState.Ready -> {
                    cachedRates = state.rates
                    val hoursAgo = ChronoUnit.HOURS.between(Instant.ofEpochMilli(state.fetchedAt), Instant.now())
                    val updatedText = if (state.isOffline) {
                        "Rates updated ${hoursAgo}h ago (offline)"
                    } else {
                        "Rates updated just now"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = state.isOffline,
                            lastUpdatedText = updatedText,
                            isNoData = false,
                        )
                    }
                    calculateConversion()
                }
                RatesState.NoData -> {
                    _uiState.update { it.copy(isLoading = false, isNoData = true) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onDigit(d: String) {
        val curr = _uiState.value.fromValue
        val next = if (curr == "0") d else curr + d
        _uiState.update { it.copy(fromValue = next) }
        calculateConversion()
    }

    fun onDecimal() {
        val curr = _uiState.value.fromValue
        if (!curr.contains(".")) {
            val next = if (curr.isEmpty()) "0." else "$curr."
            _uiState.update { it.copy(fromValue = next) }
            calculateConversion()
        }
    }

    fun onDelete() {
        val curr = _uiState.value.fromValue
        val next = if (curr.isNotEmpty()) curr.dropLast(1) else ""
        _uiState.update { it.copy(fromValue = next) }
        calculateConversion()
    }

    fun onClear() {
        _uiState.update { it.copy(fromValue = "0") }
        calculateConversion()
    }

    fun swapCurrencies() {
        _uiState.update {
            it.copy(
                fromCode = it.toCode,
                toCode = it.fromCode,
                fromValue = it.toValue.ifEmpty { "1" },
            )
        }
        calculateConversion()
    }

    fun setFromCurrency(code: String) {
        _uiState.update { it.copy(fromCode = code) }
        calculateConversion()
    }

    fun setToCurrency(code: String) {
        _uiState.update { it.copy(toCode = code) }
        calculateConversion()
    }

    private fun calculateConversion() {
        val s = _uiState.value
        val amount = s.fromValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (amount <= BigDecimal.ZERO || cachedRates.isEmpty()) return

        // Cross rate relative to base USD: amount * rate[to] / rate[from]
        val rateFrom = cachedRates[s.fromCode] ?: BigDecimal.ONE
        val rateTo = cachedRates[s.toCode] ?: BigDecimal.ONE

        val converted = amount.multiply(rateTo, MathContext.DECIMAL128)
            .divide(rateFrom, 4, RoundingMode.HALF_UP)

        _uiState.update {
            it.copy(toValue = NumberFormatter.format(converted, precision = 4, grouping = DigitGrouping.None))
        }
    }
}
