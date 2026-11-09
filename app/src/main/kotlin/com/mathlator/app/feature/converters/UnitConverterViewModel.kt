package com.mathlator.app.feature.converters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.core.format.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class ActiveInputCard { FROM, TO }

data class ConvertedRow(
    val unit: UnitDef,
    val value: String,
)

data class UnitConverterUiState(
    val category: ConverterCategory = ConverterCategory.Length,
    val availableUnits: List<UnitDef> = emptyList(),
    val fromUnit: UnitDef = ConverterCatalog.LENGTH[6], // mi
    val toUnit: UnitDef = ConverterCatalog.LENGTH[3],   // km
    val fromValue: String = "1",
    val toValue: String = "1.609344",
    val activeCard: ActiveInputCard = ActiveInputCard.FROM,
    val isBinaryData: Boolean = false,
    val allUnitsValues: List<ConvertedRow> = emptyList(),
    val isAllUnitsExpanded: Boolean = false,
    // Temperature specific
    val fromTemp: TempUnit = TempUnit.Celsius,
    val toTemp: TempUnit = TempUnit.Fahrenheit,
)

@HiltViewModel
class UnitConverterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryArg: String = savedStateHandle["category"] ?: "length"
    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    init {
        val cat = ConverterCategory.entries.find { it.id == categoryArg } ?: ConverterCategory.Length
        setCategory(cat)
    }

    fun setCategory(cat: ConverterCategory) {
        val units = when (cat) {
            ConverterCategory.Length -> ConverterCatalog.LENGTH
            ConverterCategory.Area -> ConverterCatalog.AREA
            ConverterCategory.Volume -> ConverterCatalog.VOLUME
            ConverterCategory.Mass -> ConverterCatalog.MASS
            ConverterCategory.Speed -> ConverterCatalog.SPEED
            ConverterCategory.Time -> ConverterCatalog.TIME
            ConverterCategory.Data -> ConverterCatalog.DATA_DECIMAL
            ConverterCategory.Temperature -> emptyList()
        }

        val defaultFrom = when (cat) {
            ConverterCategory.Length -> units.firstOrNull { it.id == "mi" } ?: units.first()
            ConverterCategory.Temperature -> UnitDef("c", "°C", "Celsius", BigDecimal.ONE)
            else -> units.firstOrNull() ?: UnitDef("m", "m", "Metre", BigDecimal.ONE)
        }
        val defaultTo = when (cat) {
            ConverterCategory.Length -> units.firstOrNull { it.id == "km" } ?: units[1]
            ConverterCategory.Temperature -> UnitDef("f", "°F", "Fahrenheit", BigDecimal.ONE)
            else -> if (units.size > 1) units[1] else defaultFrom
        }

        _uiState.update {
            it.copy(
                category = cat,
                availableUnits = units,
                fromUnit = defaultFrom,
                toUnit = defaultTo,
                fromValue = "1",
                toValue = "",
                activeCard = ActiveInputCard.FROM,
            )
        }
        recalculate(fromChanged = true)
    }

    fun onDigit(d: String) {
        val current = if (_uiState.value.activeCard == ActiveInputCard.FROM) _uiState.value.fromValue else _uiState.value.toValue
        val next = if (current == "0") d else current + d
        updateActiveValue(next)
    }

    fun onDecimal() {
        val current = if (_uiState.value.activeCard == ActiveInputCard.FROM) _uiState.value.fromValue else _uiState.value.toValue
        if (!current.contains(".")) {
            val next = if (current.isEmpty()) "0." else "$current."
            updateActiveValue(next)
        }
    }

    fun onSign() {
        val current = if (_uiState.value.activeCard == ActiveInputCard.FROM) _uiState.value.fromValue else _uiState.value.toValue
        val next = if (current.startsWith("-")) current.drop(1) else "-$current"
        updateActiveValue(next)
    }

    fun onDelete() {
        val current = if (_uiState.value.activeCard == ActiveInputCard.FROM) _uiState.value.fromValue else _uiState.value.toValue
        val next = if (current.isNotEmpty()) current.dropLast(1) else ""
        updateActiveValue(next)
    }

    fun onClear() {
        updateActiveValue("0")
    }

    fun setActiveCard(card: ActiveInputCard) {
        _uiState.update { it.copy(activeCard = card) }
    }

    fun swapUnits() {
        _uiState.update {
            if (it.category == ConverterCategory.Temperature) {
                it.copy(
                    fromTemp = it.toTemp,
                    toTemp = it.fromTemp,
                    fromValue = it.toValue,
                )
            } else {
                it.copy(
                    fromUnit = it.toUnit,
                    toUnit = it.fromUnit,
                    fromValue = it.toValue,
                )
            }
        }
        recalculate(fromChanged = true)
    }

    fun setFromUnit(unit: UnitDef) {
        _uiState.update { it.copy(fromUnit = unit) }
        recalculate(fromChanged = true)
    }

    fun setToUnit(unit: UnitDef) {
        _uiState.update { it.copy(toUnit = unit) }
        recalculate(fromChanged = true)
    }

    fun setFromTemp(temp: TempUnit) {
        _uiState.update { it.copy(fromTemp = temp) }
        recalculate(fromChanged = true)
    }

    fun setToTemp(temp: TempUnit) {
        _uiState.update { it.copy(toTemp = temp) }
        recalculate(fromChanged = true)
    }

    fun toggleBinaryData(isBinary: Boolean) {
        val units = if (isBinary) ConverterCatalog.DATA_BINARY else ConverterCatalog.DATA_DECIMAL
        _uiState.update {
            it.copy(
                isBinaryData = isBinary,
                availableUnits = units,
                fromUnit = units.find { u -> u.id.equals(it.fromUnit.id, ignoreCase = true) } ?: units.first(),
                toUnit = units.find { u -> u.id.equals(it.toUnit.id, ignoreCase = true) } ?: units[1],
            )
        }
        recalculate(fromChanged = true)
    }

    fun toggleAllUnitsExpanded() {
        _uiState.update { it.copy(isAllUnitsExpanded = !it.isAllUnitsExpanded) }
    }

    private fun updateActiveValue(value: String) {
        val isFrom = _uiState.value.activeCard == ActiveInputCard.FROM
        _uiState.update {
            if (isFrom) it.copy(fromValue = value) else it.copy(toValue = value)
        }
        recalculate(fromChanged = isFrom)
    }

    private fun recalculate(fromChanged: Boolean) {
        val state = _uiState.value
        val inputStr = if (fromChanged) state.fromValue else state.toValue
        val bdVal = inputStr.toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (state.category == ConverterCategory.Temperature) {
            val fromT = if (fromChanged) state.fromTemp else state.toTemp
            val toT = if (fromChanged) state.toTemp else state.fromTemp
            val converted = ConverterCatalog.convertTemperature(bdVal, fromT, toT)
            val resultStr = formatConverted(converted)

            val allTemps = TempUnit.entries.map { t ->
                val c = ConverterCatalog.convertTemperature(bdVal, state.fromTemp, t)
                ConvertedRow(
                    unit = UnitDef(t.name, t.symbol, t.name, BigDecimal.ONE),
                    value = formatConverted(c),
                )
            }

            _uiState.update {
                if (fromChanged) it.copy(toValue = resultStr, allUnitsValues = allTemps)
                else it.copy(fromValue = resultStr, allUnitsValues = allTemps)
            }
        } else {
            val fromU = if (fromChanged) state.fromUnit else state.toUnit
            val toU = if (fromChanged) state.toUnit else state.fromUnit
            val converted = ConverterCatalog.convert(bdVal, fromU, toU)
            val resultStr = formatConverted(converted)

            // Recalculate all units values
            val allRows = state.availableUnits.map { u ->
                val v = ConverterCatalog.convert(bdVal, state.fromUnit, u)
                ConvertedRow(unit = u, value = formatConverted(v))
            }

            _uiState.update {
                if (fromChanged) it.copy(toValue = resultStr, allUnitsValues = allRows)
                else it.copy(fromValue = resultStr, allUnitsValues = allRows)
            }
        }
    }

    private fun formatConverted(bd: BigDecimal): String {
        return NumberFormatter.format(
            value = bd,
            precision = 8,
            grouping = DigitGrouping.None,
        )
    }
}
