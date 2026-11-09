package com.mathlator.app.core.format

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/** Unit tests for [NumberFormatter]. */
class NumberFormatterTest {

    // ── Digit grouping ─────────────────────────────────────────────────────────

    @Test fun `indian grouping 1234567`() {
        val result = NumberFormatter.format(BigDecimal("1234567"), grouping = DigitGrouping.Indian)
        assertEquals("12,34,567", result)
    }

    @Test fun `international grouping 1234567`() {
        val result = NumberFormatter.format(BigDecimal("1234567"), grouping = DigitGrouping.International)
        assertEquals("1,234,567", result)
    }

    @Test fun `indian grouping with decimals`() {
        val result = NumberFormatter.format(BigDecimal("1234567.89"), grouping = DigitGrouping.Indian)
        assertEquals("12,34,567.89", result)
    }

    @Test fun `international grouping with decimals`() {
        val result = NumberFormatter.format(BigDecimal("1234567.89"), grouping = DigitGrouping.International)
        assertEquals("1,234,567.89", result)
    }

    @Test fun `no grouping`() {
        val result = NumberFormatter.format(BigDecimal("1234567.89"), grouping = DigitGrouping.None)
        assertEquals("1234567.89", result)
    }

    @Test fun `negative indian grouping`() {
        val result = NumberFormatter.format(BigDecimal("-1234567"), grouping = DigitGrouping.Indian)
        assertEquals("-12,34,567", result)
    }

    @Test fun `small number no grouping needed`() {
        val result = NumberFormatter.format(BigDecimal("123"), grouping = DigitGrouping.Indian)
        assertEquals("123", result)
    }

    @Test fun `trailing zeros stripped`() {
        val result = NumberFormatter.format(BigDecimal("1.5000"), grouping = DigitGrouping.None)
        assertEquals("1.5", result)
    }

    @Test fun `zero formats correctly`() {
        val result = NumberFormatter.format(BigDecimal.ZERO, grouping = DigitGrouping.Indian)
        assertEquals("0", result)
    }

    @Test fun `precision applied`() {
        val result = NumberFormatter.format(BigDecimal("1.123456789123"), precision = 10, grouping = DigitGrouping.None)
        assertEquals("1.1234567891", result)
        val dotIdx = result.indexOf('.')
        if (dotIdx >= 0) assertTrue(result.length - dotIdx - 1 <= 10)
    }

    // ── groupIndian / groupInternational unit tests ────────────────────────────

    @Test fun `groupIndian edge 3 digits`() {
        assertEquals("123", NumberFormatter.groupIndian("123"))
    }

    @Test fun `groupIndian 4 digits`() {
        assertEquals("1,234", NumberFormatter.groupIndian("1234"))
    }

    @Test fun `groupIndian 7 digits`() {
        assertEquals("12,34,567", NumberFormatter.groupIndian("1234567"))
    }

    @Test fun `groupInternational 7 digits`() {
        assertEquals("1,234,567", NumberFormatter.groupInternational("1234567"))
    }
}
