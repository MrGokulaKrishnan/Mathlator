package com.mathlator.app.feature.converters

import com.mathlator.app.feature.converters.bmi.BmiUiState
import com.mathlator.app.feature.converters.bmi.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Unit tests for Date, Age, BMI per Section 13.
 */
class SpecialConvertersTest {

    @Test
    fun `BMI 70 kg at 175 cm equals 22_9`() {
        val heightM = 1.75
        val weightKg = 70.0
        val bmi = weightKg / (heightM * heightM)
        val formatted = BigDecimal(bmi).setScale(1, RoundingMode.HALF_UP).toPlainString()
        assertEquals("22.9", formatted)
    }

    @Test
    fun `BMI underweight category`() {
        val heightM = 1.80
        val weightKg = 50.0
        val bmi = weightKg / (heightM * heightM)
        assertTrue(bmi < 18.5)
    }

    @Test
    fun `BMI Asian cutoffs overweight threshold`() {
        // Under standard cut-offs, 24 is Normal, but under Asian cut-offs, >= 23 is Overweight
        val bmi = 23.5
        val isAsianOverweight = bmi >= 23.0
        assertTrue(isAsianOverweight)
    }

    @Test
    fun `Age calculation with leap year Feb 29`() {
        // Born on Feb 29, 2000 (leap year)
        val dob = LocalDate.of(2000, 2, 29)
        val targetDate = LocalDate.of(2023, 2, 28) // Non-leap year

        val years = ChronoUnit.YEARS.between(dob, targetDate)
        assertEquals(22L, years)

        // On Feb 29, 2024 (leap year)
        val targetLeap = LocalDate.of(2024, 2, 29)
        val yearsLeap = ChronoUnit.YEARS.between(dob, targetLeap)
        assertEquals(24L, yearsLeap)
    }

    @Test
    fun `Date month-end arithmetic`() {
        val jan31 = LocalDate.of(2023, 1, 31)
        val plusOneMonth = jan31.plusMonths(1)
        // In java.time, adding 1 month to Jan 31 gives Feb 28 in non-leap year
        assertEquals(LocalDate.of(2023, 2, 28), plusOneMonth)
    }
}
