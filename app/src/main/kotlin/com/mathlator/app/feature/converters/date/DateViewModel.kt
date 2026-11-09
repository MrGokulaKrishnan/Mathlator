package com.mathlator.app.feature.converters.date

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class DateTab { DIFFERENCE, ADD_SUBTRACT }

data class DateDifferenceResult(
    val years: Int = 0,
    val months: Int = 0,
    val days: Int = 0,
    val totalDays: Long = 0,
    val weeks: Long = 0,
    val remainingDays: Long = 0,
)

data class DateUiState(
    val selectedTab: DateTab = DateTab.DIFFERENCE,
    // Difference tab
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(1),
    val differenceResult: DateDifferenceResult = DateDifferenceResult(),
    // Add/Subtract tab
    val baseDate: LocalDate = LocalDate.now(),
    val isAdd: Boolean = true,
    val yearsDelta: Int = 0,
    val monthsDelta: Int = 0,
    val daysDelta: Int = 0,
    val resultingDate: LocalDate = LocalDate.now(),
    val resultingWeekday: String = "",
)

@HiltViewModel
class DateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DateUiState())
    val uiState: StateFlow<DateUiState> = _uiState.asStateFlow()

    init {
        computeDifference()
        computeAddSubtract()
    }

    fun selectTab(tab: DateTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
        computeDifference()
    }

    fun setEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
        computeDifference()
    }

    fun setBaseDate(date: LocalDate) {
        _uiState.update { it.copy(baseDate = date) }
        computeAddSubtract()
    }

    fun setIsAdd(isAdd: Boolean) {
        _uiState.update { it.copy(isAdd = isAdd) }
        computeAddSubtract()
    }

    fun setYearsDelta(years: Int) {
        _uiState.update { it.copy(yearsDelta = years.coerceAtLeast(0)) }
        computeAddSubtract()
    }

    fun setMonthsDelta(months: Int) {
        _uiState.update { it.copy(monthsDelta = months.coerceAtLeast(0)) }
        computeAddSubtract()
    }

    fun setDaysDelta(days: Int) {
        _uiState.update { it.copy(daysDelta = days.coerceAtLeast(0)) }
        computeAddSubtract()
    }

    private fun computeDifference() {
        val s = _uiState.value.startDate
        val e = _uiState.value.endDate
        val (early, late) = if (s.isBefore(e)) s to e else e to s

        val period = Period.between(early, late)
        val totalDays = ChronoUnit.DAYS.between(early, late)
        val weeks = totalDays / 7
        val remDays = totalDays % 7

        _uiState.update {
            it.copy(
                differenceResult = DateDifferenceResult(
                    years = period.years,
                    months = period.months,
                    days = period.days,
                    totalDays = totalDays,
                    weeks = weeks,
                    remainingDays = remDays,
                )
            )
        }
    }

    private fun computeAddSubtract() {
        val base = _uiState.value.baseDate
        val isAdd = _uiState.value.isAdd
        val y = _uiState.value.yearsDelta.toLong()
        val m = _uiState.value.monthsDelta.toLong()
        val d = _uiState.value.daysDelta.toLong()

        val resultDate = if (isAdd) {
            base.plusYears(y).plusMonths(m).plusDays(d)
        } else {
            base.minusYears(y).minusMonths(m).minusDays(d)
        }

        val weekday = resultDate.format(DateTimeFormatter.ofPattern("EEEE"))
        _uiState.update {
            it.copy(
                resultingDate = resultDate,
                resultingWeekday = weekday,
            )
        }
    }
}
