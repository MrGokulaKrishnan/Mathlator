package com.mathlator.app.feature.finance

import com.mathlator.app.core.finance.CurrencyCalculator
import com.mathlator.app.core.format.DigitGrouping
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CurrencyConversionTest {

    private val rates = mapOf(
        "USD" to BigDecimal("1.0"),
        "INR" to BigDecimal("90.0"),
        "EUR" to BigDecimal("0.9"),
    )

    @Test
    fun testConversion900InrToUsdAndEur() {
        val amount = BigDecimal("900")

        // 900 INR → 10 USD
        val usdVal = CurrencyCalculator.convert(amount, fromCode = "INR", toCode = "USD", rates = rates)
        val usdFormatted = CurrencyCalculator.formatConverted(usdVal, "USD", DigitGrouping.None)
        assertEquals("10", usdFormatted)

        // 900 INR → 9 EUR
        val eurVal = CurrencyCalculator.convert(amount, fromCode = "INR", toCode = "EUR", rates = rates)
        val eurFormatted = CurrencyCalculator.formatConverted(eurVal, "EUR", DigitGrouping.None)
        assertEquals("9", eurFormatted)
    }

    @Test
    fun testPromoteUsdToMain() {
        // Initial setup: main = INR, out1 = USD, out2 = EUR
        // Current values: main = 900, out1 = 10, out2 = 9
        val (newCurrencies, newMainVal) = CurrencyCalculator.promoteToMain(
            promotedSlot = 1, // OUT1 (USD) is promoted
            currentMain = "INR",
            currentOut1 = "USD",
            currentOut2 = "EUR",
            out1Value = "10",
            out2Value = "9",
        )

        // USD becomes main, INR takes out1 slot, EUR remains out2
        assertEquals(Triple("USD", "INR", "EUR"), newCurrencies)
        assertEquals("10", newMainVal)

        // Calculate conversions with new main = 10 USD:
        val mainAmt = BigDecimal(newMainVal)
        val inrVal = CurrencyCalculator.convert(mainAmt, fromCode = newCurrencies.first, toCode = newCurrencies.second, rates = rates)
        val inrFormatted = CurrencyCalculator.formatConverted(inrVal, "INR", DigitGrouping.None)
        assertEquals("900", inrFormatted)

        val eurVal = CurrencyCalculator.convert(mainAmt, fromCode = newCurrencies.first, toCode = newCurrencies.third, rates = rates)
        val eurFormatted = CurrencyCalculator.formatConverted(eurVal, "EUR", DigitGrouping.None)
        assertEquals("9", eurFormatted)
    }

    @Test
    fun testChoosingEurForInrSlotSwapsInsteadOfDuplicating() {
        // Current slots: main = INR, out1 = USD, out2 = EUR
        // User chooses EUR for slot 0 (main, where INR currently is)
        val swapped = CurrencyCalculator.swapSlots(
            targetSlot = 0,
            code = "EUR",
            currentMain = "INR",
            currentOut1 = "USD",
            currentOut2 = "EUR",
        )

        // EUR was in out2, so main becomes EUR and out2 becomes INR; no duplicate EUR
        assertEquals(Triple("EUR", "USD", "INR"), swapped)
    }

    @Test
    fun testSwapSlotForOut1WhenAlreadyInUse() {
        // Current slots: main = INR, out1 = USD, out2 = EUR
        // User chooses EUR for slot 1 (out1, where USD is)
        val swapped = CurrencyCalculator.swapSlots(
            targetSlot = 1,
            code = "EUR",
            currentMain = "INR",
            currentOut1 = "USD",
            currentOut2 = "EUR",
        )

        // EUR was in out2, so out1 becomes EUR and out2 becomes USD
        assertEquals(Triple("INR", "EUR", "USD"), swapped)
    }

    @Test
    fun testSwapSlotForOut2WhenAlreadyInUse() {
        // Current slots: main = INR, out1 = USD, out2 = EUR
        // User chooses INR for slot 2 (out2, where EUR is)
        val swapped = CurrencyCalculator.swapSlots(
            targetSlot = 2,
            code = "INR",
            currentMain = "INR",
            currentOut1 = "USD",
            currentOut2 = "EUR",
        )

        // INR was in main, so out2 becomes INR and main becomes EUR
        assertEquals(Triple("EUR", "USD", "INR"), swapped)
    }

    @Test
    fun testRegionDefaultCurrenciesLogic() {
        // Verify default currencies logic
        val defaultsInr = com.mathlator.app.data.prefs.SettingsRepository.getRegionDefaultCurrencies()
        // Must contain 3 distinct currencies
        val list = listOf(defaultsInr.first, defaultsInr.second, defaultsInr.third)
        assertEquals(3, list.distinct().size)
    }
}
