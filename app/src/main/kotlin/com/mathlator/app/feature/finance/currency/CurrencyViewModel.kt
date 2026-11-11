package com.mathlator.app.feature.finance.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathlator.app.core.finance.CurrencyCalculator
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.data.rates.RatesRepository
import com.mathlator.app.data.rates.RatesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
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
    val mainCode: String = "INR",
    val out1Code: String = "USD",
    val out2Code: String = "EUR",
    val mainValue: String = "100",
    val out1Value: String = "",
    val out2Value: String = "",
    val out1Rate: String = "",
    val out2Rate: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val lastUpdatedText: String = "",
    val isNoData: Boolean = false,
    val attributionText: String = "Rates from open.er-api.com",
)

/** Which card's picker is showing (null = none). */
enum class PickerTarget { MAIN, OUT1, OUT2 }

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val ratesRepository: RatesRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    private var cachedRates: Map<String, BigDecimal> = emptyMap()
    private var currentGrouping: DigitGrouping = DigitGrouping.Indian

    val currencyList: List<CurrencyItem> = listOf(
        CurrencyItem("INR", "Indian Rupee",        "🇮🇳", isPinned = true),
        CurrencyItem("USD", "US Dollar",            "🇺🇸", isPinned = true),
        CurrencyItem("EUR", "Euro",                 "🇪🇺", isPinned = true),
        CurrencyItem("GBP", "British Pound",        "🇬🇧", isPinned = true),
        CurrencyItem("JPY", "Japanese Yen",         "🇯🇵", isPinned = true),
        CurrencyItem("AUD", "Australian Dollar",    "🇦🇺", isPinned = true),
        CurrencyItem("CAD", "Canadian Dollar",      "🇨🇦", isPinned = true),
        CurrencyItem("CHF", "Swiss Franc",          "🇨🇭", isPinned = true),
        CurrencyItem("CNY", "Chinese Yuan",         "🇨🇳", isPinned = true),
        CurrencyItem("SGD", "Singapore Dollar",     "🇸🇬", isPinned = true),
        CurrencyItem("AED", "UAE Dirham",           "🇦🇪", isPinned = true),
        CurrencyItem("SAR", "Saudi Riyal",          "🇸🇦"),
        CurrencyItem("NZD", "New Zealand Dollar",   "🇳🇿"),
        CurrencyItem("KRW", "South Korean Won",     "🇰🇷"),
        CurrencyItem("BRL", "Brazilian Real",       "🇧🇷"),
        CurrencyItem("ZAR", "South African Rand",   "🇿🇦"),
        CurrencyItem("MXN", "Mexican Peso",         "🇲🇽"),
        CurrencyItem("THB", "Thai Baht",            "🇹🇭"),
        CurrencyItem("MYR", "Malaysian Ringgit",    "🇲🇾"),
        CurrencyItem("KWD", "Kuwaiti Dinar",        "🇰🇼"),
    )

    init {
        viewModelScope.launch {
            combine(
                settings.currencyFrom,
                settings.currencyTo,
                settings.currencyOut2,
                settings.digitGrouping,
            ) { main, out1, out2, grouping ->
                Tuple4(main, out1, out2, grouping)
            }.collect { (main, out1, out2, grouping) ->
                currentGrouping = grouping
                _uiState.update { it.copy(mainCode = main, out1Code = out1, out2Code = out2) }
                calculateConversions()
            }
        }
        loadRates(force = false)
    }

    fun loadRates(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isNoData = false) }
            when (val state = ratesRepository.refresh(base = "USD", force = force)) {
                is RatesState.Ready -> {
                    cachedRates = state.rates
                    val hoursAgo = ChronoUnit.HOURS.between(
                        Instant.ofEpochMilli(state.fetchedAt), Instant.now()
                    )
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
                    calculateConversions()
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
        val curr = _uiState.value.mainValue
        val next = when {
            curr == "0" && d != "0" -> d
            curr == "0" && d == "0" -> "0"
            else -> curr + d
        }
        _uiState.update { it.copy(mainValue = next) }
        calculateConversions()
    }

    fun onDecimal() {
        val curr = _uiState.value.mainValue
        if (!curr.contains(".")) {
            val next = if (curr.isEmpty()) "0." else "$curr."
            _uiState.update { it.copy(mainValue = next) }
            calculateConversions()
        }
    }

    fun onDelete() {
        val curr = _uiState.value.mainValue
        val next = if (curr.isNotEmpty()) curr.dropLast(1) else ""
        _uiState.update { it.copy(mainValue = if (next.isEmpty()) "0" else next) }
        calculateConversions()
    }

    fun onClear() {
        _uiState.update { it.copy(mainValue = "0") }
        calculateConversions()
    }

    /**
     * Set a currency for the given target slot.
     * If [code] is already used in another slot, swap those two slots.
     */
    fun setCurrency(target: PickerTarget, code: String) {
        val s = _uiState.value
        val slot = when (target) {
            PickerTarget.MAIN -> 0
            PickerTarget.OUT1 -> 1
            PickerTarget.OUT2 -> 2
        }
        val (newMain, newOut1, newOut2) = CurrencyCalculator.swapSlots(
            slot, code, s.mainCode, s.out1Code, s.out2Code
        )
        viewModelScope.launch {
            if (newMain != s.mainCode) settings.setCurrencyFrom(newMain)
            if (newOut1 != s.out1Code) settings.setCurrencyTo(newOut1)
            if (newOut2 != s.out2Code) settings.setCurrencyOut2(newOut2)
        }
    }

    /**
     * Promote a converted card to MAIN.
     * The promoted card's currency becomes MAIN; MAIN's currency moves to that slot.
     * The typed amount becomes the promoted card's current displayed value.
     */
    fun promoteToMain(target: PickerTarget) {
        val s = _uiState.value
        val slot = when (target) {
            PickerTarget.OUT1 -> 1
            PickerTarget.OUT2 -> 2
            PickerTarget.MAIN -> return
        }
        val (newCurrencies, newMainVal) = CurrencyCalculator.promoteToMain(
            slot, s.mainCode, s.out1Code, s.out2Code, s.out1Value, s.out2Value
        )
        viewModelScope.launch {
            if (newCurrencies.first != s.mainCode) settings.setCurrencyFrom(newCurrencies.first)
            if (newCurrencies.second != s.out1Code) settings.setCurrencyTo(newCurrencies.second)
            if (newCurrencies.third != s.out2Code) settings.setCurrencyOut2(newCurrencies.third)
            _uiState.update { it.copy(mainValue = newMainVal) }
            calculateConversions()
        }
    }

    private fun calculateConversions() {
        val s = _uiState.value
        val amount = s.mainValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (cachedRates.isEmpty()) return

        val converted1 = CurrencyCalculator.convert(amount, s.mainCode, s.out1Code, cachedRates)
        val converted2 = CurrencyCalculator.convert(amount, s.mainCode, s.out2Code, cachedRates)

        val out1 = CurrencyCalculator.formatConverted(converted1, s.out1Code, currentGrouping)
        val out2 = CurrencyCalculator.formatConverted(converted2, s.out2Code, currentGrouping)

        val rate1 = CurrencyCalculator.rateCaption(s.mainCode, s.out1Code, cachedRates)
        val rate2 = CurrencyCalculator.rateCaption(s.mainCode, s.out2Code, cachedRates)

        _uiState.update {
            it.copy(
                out1Value = out1,
                out2Value = out2,
                out1Rate = rate1,
                out2Rate = rate2,
            )
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
