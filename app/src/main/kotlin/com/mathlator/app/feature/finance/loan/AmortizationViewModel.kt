package com.mathlator.app.feature.finance.loan

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mathlator.app.core.finance.AmortizationEntry
import com.mathlator.app.core.finance.EmiCalculator
import com.mathlator.app.core.format.NumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.math.BigDecimal
import javax.inject.Inject

enum class AmortizationViewMode { MONTHLY, YEARLY }

data class YearlyAmortizationEntry(
    val year: Int,
    val emi: BigDecimal,
    val principal: BigDecimal,
    val interest: BigDecimal,
    val endingBalance: BigDecimal,
)

data class AmortizationUiState(
    val viewMode: AmortizationViewMode = AmortizationViewMode.MONTHLY,
    val monthlySchedule: List<AmortizationEntry> = emptyList(),
    val yearlySchedule: List<YearlyAmortizationEntry> = emptyList(),
)

@HiltViewModel
class AmortizationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val amountStr: String = savedStateHandle["amount"] ?: "1000000"
    private val rateStr: String = savedStateHandle["rate"] ?: "9.0"
    private val monthsInt: Int = savedStateHandle.get<String>("months")?.toIntOrNull() ?: 240

    private val _uiState = MutableStateFlow(AmortizationUiState())
    val uiState: StateFlow<AmortizationUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun setViewMode(mode: AmortizationViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    private fun loadSchedule() {
        val principal = amountStr.toBigDecimalOrNull() ?: BigDecimal("1000000")
        val rate = rateStr.toBigDecimalOrNull() ?: BigDecimal("9.0")

        val monthly = EmiCalculator.amortizationSchedule(
            principal = principal,
            annualRatePct = rate,
            tenureMonths = monthsInt,
        )

        // Aggregate by Year
        val yearly = monthly.groupBy { (it.month - 1) / 12 + 1 }
            .map { (year, entries) ->
                YearlyAmortizationEntry(
                    year = year,
                    emi = entries.fold(BigDecimal.ZERO) { acc, e -> acc + e.emi },
                    principal = entries.fold(BigDecimal.ZERO) { acc, e -> acc + e.principal },
                    interest = entries.fold(BigDecimal.ZERO) { acc, e -> acc + e.interest },
                    endingBalance = entries.last().balance,
                )
            }

        _uiState.update {
            it.copy(
                monthlySchedule = monthly,
                yearlySchedule = yearly,
            )
        }
    }

    fun shareCsv() {
        try {
            val dir = File(context.filesDir, "amortization").apply { mkdirs() }
            val file = File(dir, "amortization_schedule.csv")

            file.bufferedWriter().use { writer ->
                writer.write("Period,EMI,Principal,Interest,Balance\n")
                if (_uiState.value.viewMode == AmortizationViewMode.MONTHLY) {
                    _uiState.value.monthlySchedule.forEach { e ->
                        writer.write("${e.month},${e.emi},${e.principal},${e.interest},${e.balance}\n")
                    }
                } else {
                    _uiState.value.yearlySchedule.forEach { e ->
                        writer.write("Year ${e.year},${e.emi},${e.principal},${e.interest},${e.endingBalance}\n")
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Share Amortization Schedule").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Handle error silently
        }
    }
}
