package com.mathlator.app.feature.converters.age

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

data class AgeResult(
    val years: Int = 0,
    val months: Int = 0,
    val days: Int = 0,
    val totalMonths: Long = 0,
    val totalWeeks: Long = 0,
    val totalDays: Long = 0,
    val totalHours: Long = 0,
    val nextBirthdayDate: LocalDate = LocalDate.now(),
    val nextBirthdayWeekday: String = "",
    val nextBirthdayDaysRemaining: Long = 0,
)

data class AgeUiState(
    val dob: LocalDate = LocalDate.of(2000, 1, 1),
    val ageAt: LocalDate = LocalDate.now(),
    val isValid: Boolean = true,
    val errorMessage: String = "",
    val result: AgeResult = AgeResult(),
)

@HiltViewModel
class AgeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AgeUiState())
    val uiState: StateFlow<AgeUiState> = _uiState.asStateFlow()

    init {
        calculateAge()
    }

    fun setDob(date: LocalDate) {
        _uiState.update { it.copy(dob = date) }
        calculateAge()
    }

    fun setAgeAt(date: LocalDate) {
        _uiState.update { it.copy(ageAt = date) }
        calculateAge()
    }

    private fun calculateAge() {
        val dob = _uiState.value.dob
        val ageAt = _uiState.value.ageAt

        if (dob.isAfter(ageAt)) {
            _uiState.update {
                it.copy(
                    isValid = false,
                    errorMessage = "Date of birth cannot be after target date",
                )
            }
            return
        }

        val period = Period.between(dob, ageAt)
        val totalDays = ChronoUnit.DAYS.between(dob, ageAt)
        val totalMonths = ChronoUnit.MONTHS.between(dob, ageAt)
        val totalWeeks = totalDays / 7
        val totalHours = totalDays * 24

        // Next birthday calculation, handling Feb 29
        val nextBday = calculateNextBirthday(dob, ageAt)
        val daysUntilNextBday = ChronoUnit.DAYS.between(ageAt, nextBday)
        val weekday = nextBday.format(DateTimeFormatter.ofPattern("EEEE"))

        _uiState.update {
            it.copy(
                isValid = true,
                errorMessage = "",
                result = AgeResult(
                    years = period.years,
                    months = period.months,
                    days = period.days,
                    totalMonths = totalMonths,
                    totalWeeks = totalWeeks,
                    totalDays = totalDays,
                    totalHours = totalHours,
                    nextBirthdayDate = nextBday,
                    nextBirthdayWeekday = weekday,
                    nextBirthdayDaysRemaining = daysUntilNextBday,
                )
            )
        }
    }

    private fun calculateNextBirthday(dob: LocalDate, fromDate: LocalDate): LocalDate {
        var targetYear = fromDate.year
        val candidate = resolveBirthdayInYear(dob, targetYear)
        return if (candidate.isBefore(fromDate) || candidate.isEqual(fromDate)) {
            resolveBirthdayInYear(dob, targetYear + 1)
        } else {
            candidate
        }
    }

    private fun resolveBirthdayInYear(dob: LocalDate, year: Int): LocalDate {
        return if (dob.monthValue == 2 && dob.dayOfMonth == 29) {
            val isLeap = java.time.Year.of(year).isLeap
            if (isLeap) LocalDate.of(year, 2, 29) else LocalDate.of(year, 2, 28)
        } else {
            LocalDate.of(year, dob.month, dob.dayOfMonth)
        }
    }
}
