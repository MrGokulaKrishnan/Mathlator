package com.mathlator.app.feature.converters.numeral

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigInteger
import javax.inject.Inject

enum class Radix(val base: Int, val label: String) {
    BIN(2, "BIN"),
    OCT(8, "OCT"),
    DEC(10, "DEC"),
    HEX(16, "HEX"),
}

data class NumeralUiState(
    val activeRadix: Radix = Radix.DEC,
    val binValue: String = "0",
    val octValue: String = "0",
    val decValue: String = "0",
    val hexValue: String = "0",
)

@HiltViewModel
class NumeralViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NumeralUiState())
    val uiState: StateFlow<NumeralUiState> = _uiState.asStateFlow()

    fun selectRadix(radix: Radix) {
        _uiState.update { it.copy(activeRadix = radix) }
    }

    fun onChar(c: Char) {
        val active = _uiState.value.activeRadix
        val currentStr = when (active) {
            Radix.BIN -> _uiState.value.binValue
            Radix.OCT -> _uiState.value.octValue
            Radix.DEC -> _uiState.value.decValue
            Radix.HEX -> _uiState.value.hexValue
        }

        val nextStr = if (currentStr == "0") c.toString() else currentStr + c
        updateFromInput(nextStr, active)
    }

    fun onSign() {
        val active = _uiState.value.activeRadix
        val currentStr = when (active) {
            Radix.BIN -> _uiState.value.binValue
            Radix.OCT -> _uiState.value.octValue
            Radix.DEC -> _uiState.value.decValue
            Radix.HEX -> _uiState.value.hexValue
        }

        val nextStr = if (currentStr.startsWith("-")) currentStr.drop(1) else "-$currentStr"
        updateFromInput(nextStr, active)
    }

    fun onDelete() {
        val active = _uiState.value.activeRadix
        val currentStr = when (active) {
            Radix.BIN -> _uiState.value.binValue
            Radix.OCT -> _uiState.value.octValue
            Radix.DEC -> _uiState.value.decValue
            Radix.HEX -> _uiState.value.hexValue
        }

        val nextStr = if (currentStr.length > 1) currentStr.dropLast(1) else "0"
        updateFromInput(nextStr, active)
    }

    fun onClear() {
        _uiState.update {
            NumeralUiState(
                activeRadix = it.activeRadix,
                binValue = "0",
                octValue = "0",
                decValue = "0",
                hexValue = "0",
            )
        }
    }

    private fun updateFromInput(input: String, radix: Radix) {
        if (input.isEmpty() || input == "-") {
            _uiState.update {
                when (radix) {
                    Radix.BIN -> it.copy(binValue = input, octValue = "", decValue = "", hexValue = "")
                    Radix.OCT -> it.copy(octValue = input, binValue = "", decValue = "", hexValue = "")
                    Radix.DEC -> it.copy(decValue = input, binValue = "", octValue = "", hexValue = "")
                    Radix.HEX -> it.copy(hexValue = input, binValue = "", octValue = "", decValue = "")
                }
            }
            return
        }

        try {
            val isNegative = input.startsWith("-")
            val cleanStr = if (isNegative) input.drop(1) else input
            if (cleanStr.isEmpty()) return

            val bi = BigInteger(cleanStr, radix.base)
            // Limit to 128 bits
            if (bi.bitLength() > 128) return

            val signedBi = if (isNegative) bi.negate() else bi

            _uiState.update {
                it.copy(
                    binValue = signedBi.toString(2),
                    octValue = signedBi.toString(8),
                    decValue = signedBi.toString(10),
                    hexValue = signedBi.toString(16).uppercase(),
                )
            }
        } catch (_: Exception) {
            // Ignore invalid parse
        }
    }
}
