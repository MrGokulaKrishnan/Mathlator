package com.mathlator.app.feature.converters.bmi

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class UnitSystem { METRIC, IMPERIAL }

data class BmiUiState(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val heightCm: String = "175",
    val heightFt: String = "5",
    val heightIn: String = "9",
    val weightKg: String = "70",
    val weightLb: String = "154",
    val isAsianCutoffs: Boolean = false,
    val bmiValue: String = "22.9",
    val categoryLabel: String = "Normal",
    val healthyWeightRange: String = "56.7 – 76.3 kg",
)

@HiltViewModel
class BmiViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BmiUiState())
    val uiState: StateFlow<BmiUiState> = _uiState.asStateFlow()

    init {
        calculateBmi()
    }

    fun setUnitSystem(system: UnitSystem) {
        _uiState.update { it.copy(unitSystem = system) }
        calculateBmi()
    }

    fun setHeightCm(v: String) {
        _uiState.update { it.copy(heightCm = v) }
        calculateBmi()
    }

    fun setHeightFt(v: String) {
        _uiState.update { it.copy(heightFt = v) }
        calculateBmi()
    }

    fun setHeightIn(v: String) {
        _uiState.update { it.copy(heightIn = v) }
        calculateBmi()
    }

    fun setWeightKg(v: String) {
        _uiState.update { it.copy(weightKg = v) }
        calculateBmi()
    }

    fun setWeightLb(v: String) {
        _uiState.update { it.copy(weightLb = v) }
        calculateBmi()
    }

    fun setAsianCutoffs(enabled: Boolean) {
        _uiState.update { it.copy(isAsianCutoffs = enabled) }
        calculateBmi()
    }

    private fun calculateBmi() {
        val s = _uiState.value

        // Resolve height in metres and weight in kg
        val heightM: Double = if (s.unitSystem == UnitSystem.METRIC) {
            (s.heightCm.toDoubleOrNull() ?: 175.0) / 100.0
        } else {
            val ft = s.heightFt.toDoubleOrNull() ?: 5.0
            val inches = s.heightIn.toDoubleOrNull() ?: 9.0
            (ft * 12.0 + inches) * 0.0254
        }

        val weightKg: Double = if (s.unitSystem == UnitSystem.METRIC) {
            s.weightKg.toDoubleOrNull() ?: 70.0
        } else {
            (s.weightLb.toDoubleOrNull() ?: 154.0) * 0.45359237
        }

        if (heightM <= 0.0 || weightKg <= 0.0) return

        val bmi = weightKg / (heightM * heightM)
        val formattedBmi = BigDecimal(bmi).setScale(1, RoundingMode.HALF_UP).toPlainString()

        // Categories per standard or Asian cut-offs
        val category = if (s.isAsianCutoffs) {
            when {
                bmi < 18.5 -> "Underweight"
                bmi < 23.0 -> "Normal"
                bmi < 25.0 -> "Overweight"
                else -> "Obese"
            }
        } else {
            when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }

        // Healthy weight range (BMI 18.5 to 24.9)
        val minHealthyKg = 18.5 * heightM * heightM
        val maxHealthyKg = 24.9 * heightM * heightM
        val minStr = BigDecimal(minHealthyKg).setScale(1, RoundingMode.HALF_UP).toPlainString()
        val maxStr = BigDecimal(maxHealthyKg).setScale(1, RoundingMode.HALF_UP).toPlainString()

        val healthyRange = if (s.unitSystem == UnitSystem.METRIC) {
            "$minStr – $maxStr kg"
        } else {
            val minLb = BigDecimal(minHealthyKg / 0.45359237).setScale(1, RoundingMode.HALF_UP).toPlainString()
            val maxLb = BigDecimal(maxHealthyKg / 0.45359237).setScale(1, RoundingMode.HALF_UP).toPlainString()
            "$minLb – $maxLb lb"
        }

        _uiState.update {
            it.copy(
                bmiValue = formattedBmi,
                categoryLabel = category,
                healthyWeightRange = healthyRange,
            )
        }
    }
}
