package com.mathlator.app.core.format

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formats [BigDecimal] values for display in the calculator.
 *
 * Rules:
 * - Strip trailing zeros up to [precision] decimals.
 * - Switch to scientific notation when |x| ≥ 1e15 or 0 < |x| < 1e-9.
 * - Apply [DigitGrouping] to the integer part.
 * - Never use group separators for engine strings — only for display.
 */
object NumberFormatter {

    private val THRESHOLD_HIGH = BigDecimal("1e15")
    private val THRESHOLD_LOW  = BigDecimal("1e-9")

    /**
     * Formats [value] for display.
     *
     * @param value       The value to format.
     * @param precision   Maximum decimal places (default 10).
     * @param grouping    Digit grouping style.
     * @return Formatted string.
     */
    fun format(
        value: BigDecimal,
        precision: Int = 10,
        grouping: DigitGrouping = DigitGrouping.Indian,
    ): String {
        val abs = value.abs()

        // Scientific notation thresholds
        val useSci = abs >= THRESHOLD_HIGH || (abs > BigDecimal.ZERO && abs < THRESHOLD_LOW)
        if (useSci) {
            return formatScientific(value, precision)
        }

        // Round to precision, strip trailing zeros
        val rounded = value.setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = rounded.toPlainString()

        // Split into integer and fractional parts
        val dotIdx = plain.indexOf('.')
        val intPart: String
        val fracPart: String
        if (dotIdx >= 0) {
            intPart = plain.substring(0, dotIdx)
            fracPart = plain.substring(dotIdx) // includes '.'
        } else {
            intPart = plain
            fracPart = ""
        }

        return applyGrouping(intPart, grouping) + fracPart
    }

    /**
     * Applies digit grouping to the integer part of a number string.
     * Handles leading minus sign.
     */
    fun applyGrouping(intPart: String, grouping: DigitGrouping): String {
        if (grouping == DigitGrouping.None) return intPart
        val negative = intPart.startsWith("-")
        val digits = if (negative) intPart.substring(1) else intPart
        val grouped = when (grouping) {
            DigitGrouping.Indian        -> groupIndian(digits)
            DigitGrouping.International -> groupInternational(digits)
            DigitGrouping.None          -> digits
        }
        return if (negative) "-$grouped" else grouped
    }

    /**
     * Indian grouping: last 3 digits, then groups of 2.
     * E.g., 1234567 → 12,34,567.
     */
    fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits
        val result = StringBuilder()
        val remainder = digits.length - 3
        result.append(digits.substring(0, remainder % 2).ifEmpty { "" })
        var i = remainder % 2
        while (i < remainder) {
            if (result.isNotEmpty()) result.append(',')
            result.append(digits.substring(i, i + 2))
            i += 2
        }
        if (result.isNotEmpty()) result.append(',')
        result.append(digits.substring(remainder))
        return result.toString()
    }

    /**
     * International grouping: groups of 3 from the right.
     * E.g., 1234567 → 1,234,567.
     */
    fun groupInternational(digits: String): String {
        if (digits.length <= 3) return digits
        val result = StringBuilder()
        val offset = digits.length % 3
        if (offset > 0) result.append(digits.substring(0, offset))
        var i = offset
        while (i < digits.length) {
            if (result.isNotEmpty()) result.append(',')
            result.append(digits.substring(i, i + 3))
            i += 3
        }
        return result.toString()
    }

    private fun formatScientific(value: BigDecimal, precision: Int): String {
        // Use engineering-style E notation
        val rounded = value.round(java.math.MathContext(precision.coerceAtLeast(1)))
        val str = rounded.toEngineeringString()
        // Replace 'E' with our preferred format
        return str.replace("E+", "E+").replace("E-", "E-")
    }

    /**
     * Formats a [BigDecimal] monetary value with exactly 2 decimal places
     * and [RoundingMode.HALF_UP].
     */
    fun formatMoney(
        value: BigDecimal,
        grouping: DigitGrouping = DigitGrouping.Indian,
    ): String {
        val rounded = value.setScale(2, RoundingMode.HALF_UP)
        val plain = rounded.toPlainString()
        val dotIdx = plain.indexOf('.')
        val intPart = if (dotIdx >= 0) plain.substring(0, dotIdx) else plain
        val fracPart = if (dotIdx >= 0) plain.substring(dotIdx) else ".00"
        return applyGrouping(intPart, grouping) + fracPart
    }
}
