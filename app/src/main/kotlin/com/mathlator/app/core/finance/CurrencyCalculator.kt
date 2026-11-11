package com.mathlator.app.core.finance

import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.core.format.NumberFormatter
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Currency as JavaCurrency

/**
 * Pure Kotlin currency conversion logic for Mathlator.
 * Handles 3-currency conversions, slot swaps without duplicates, and promote-to-main.
 */
object CurrencyCalculator {

    /**
     * Converts [amount] from [fromCode] to [toCode] using USD-based exchange [rates].
     */
    fun convert(
        amount: BigDecimal,
        fromCode: String,
        toCode: String,
        rates: Map<String, BigDecimal>,
    ): BigDecimal {
        if (amount <= BigDecimal.ZERO || rates.isEmpty()) return BigDecimal.ZERO
        val rateFrom = rates[fromCode] ?: BigDecimal.ONE
        val rateTo = rates[toCode] ?: BigDecimal.ONE
        val digits = fractionDigits(toCode)
        return amount.multiply(rateTo, MathContext.DECIMAL128)
            .divide(rateFrom, digits.coerceAtLeast(4), RoundingMode.HALF_UP)
    }

    /**
     * Formats the converted value:
     * - Uses currency's own fraction digits (e.g. JPY 0, USD 2, KWD 3)
     * - Shows up to 4 decimals when value is < 1
     * - Applies [grouping]
     */
    fun formatConverted(
        value: BigDecimal,
        code: String,
        grouping: DigitGrouping = DigitGrouping.Indian,
    ): String {
        val digits = fractionDigits(code)
        val scale = if (value > BigDecimal.ZERO && value.compareTo(BigDecimal.ONE) < 0) 4 else digits
        return NumberFormatter.format(value, precision = scale, grouping = grouping)
    }

    /**
     * Formats the rate caption: "1 [mainCode] = X [outCode]"
     */
    fun rateCaption(
        mainCode: String,
        outCode: String,
        rates: Map<String, BigDecimal>,
    ): String {
        val rateMain = rates[mainCode] ?: BigDecimal.ONE
        val rateTo = rates[outCode] ?: BigDecimal.ONE
        val one = BigDecimal.ONE
        val x = one.multiply(rateTo, MathContext.DECIMAL128)
            .divide(rateMain, fractionDigits(outCode).coerceAtLeast(4), RoundingMode.HALF_UP)
        return "1 $mainCode = ${x.stripTrailingZeros().toPlainString()} $outCode"
    }

    /**
     * Returns the updated triple of (main, out1, out2) when [code] is picked for [targetSlot].
     * If [code] is already in another slot, the two slots swap instead of creating a duplicate.
     *
     * @param targetSlot 0 = main, 1 = out1, 2 = out2
     */
    fun swapSlots(
        targetSlot: Int,
        code: String,
        currentMain: String,
        currentOut1: String,
        currentOut2: String,
    ): Triple<String, String, String> {
        return when (targetSlot) {
            0 -> when (code) {
                currentOut1 -> Triple(currentOut1, currentMain, currentOut2)
                currentOut2 -> Triple(currentOut2, currentOut1, currentMain)
                else -> Triple(code, currentOut1, currentOut2)
            }
            1 -> when (code) {
                currentMain -> Triple(currentOut1, currentMain, currentOut2)
                currentOut2 -> Triple(currentMain, currentOut2, currentOut1)
                else -> Triple(currentMain, code, currentOut2)
            }
            2 -> when (code) {
                currentMain -> Triple(currentOut2, currentOut1, currentMain)
                currentOut1 -> Triple(currentMain, currentOut2, currentOut1)
                else -> Triple(currentMain, currentOut1, code)
            }
            else -> Triple(currentMain, currentOut1, currentOut2)
        }
    }

    /**
     * Promotes a converted card (1 for out1, 2 for out2) to main.
     * Returns the updated (main, out1, out2) currencies and the new typed main amount.
     */
    fun promoteToMain(
        promotedSlot: Int,
        currentMain: String,
        currentOut1: String,
        currentOut2: String,
        out1Value: String,
        out2Value: String,
    ): Pair<Triple<String, String, String>, String> {
        return when (promotedSlot) {
            1 -> Pair(Triple(currentOut1, currentMain, currentOut2), out1Value.ifEmpty { "0" })
            2 -> Pair(Triple(currentOut2, currentOut1, currentMain), out2Value.ifEmpty { "0" })
            else -> Pair(Triple(currentMain, currentOut1, currentOut2), "0")
        }
    }

    fun fractionDigits(code: String): Int = try {
        JavaCurrency.getInstance(code).defaultFractionDigits.coerceAtLeast(0)
    } catch (_: Exception) {
        2
    }
}
