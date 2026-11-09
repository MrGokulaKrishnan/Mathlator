package com.mathlator.app.core.finance

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/** Unit tests for finance calculators (Section 13). */
class FinanceCalculatorTest {

    // ── GST ───────────────────────────────────────────────────────────────────

    @Test fun `GST add 1000 at 18 percent`() {
        val r = GstCalculator.addGst(BigDecimal("1000"), BigDecimal("18"))
        assertEquals(BigDecimal("180.00"), r.gstAmount)
        assertEquals(BigDecimal("1180.00"), r.totalAmount)
        assertEquals(BigDecimal("1000.00"), r.baseAmount)
    }

    @Test fun `GST remove 1180 at 18 percent`() {
        val r = GstCalculator.removeGst(BigDecimal("1180"), BigDecimal("18"))
        assertEquals(BigDecimal("1000.00"), r.baseAmount)
        assertEquals(BigDecimal("180.00"), r.gstAmount)
        assertEquals(BigDecimal("1180.00"), r.totalAmount)
    }

    @Test fun `GST CGST SGST split`() {
        val r = GstCalculator.addGst(BigDecimal("1000"), BigDecimal("18"))
        assertEquals(BigDecimal("90.00"), r.cgst)
        assertEquals(BigDecimal("90.00"), r.sgst)
    }

    // ── EMI ───────────────────────────────────────────────────────────────────

    @Test fun `EMI 1000000 at 9 percent for 20 years`() {
        val r = EmiCalculator.calculate(
            principal = BigDecimal("1000000"),
            annualRatePct = BigDecimal("9"),
            tenureMonths = 240,
        )
        // Expected ≈ 8997
        val emi = r.emi.toDouble()
        assertTrue("EMI expected ≈8997, got $emi", emi in 8990.0..9010.0)
    }

    @Test fun `EMI at 0 percent`() {
        val r = EmiCalculator.calculate(
            principal = BigDecimal("120000"),
            annualRatePct = BigDecimal.ZERO,
            tenureMonths = 12,
        )
        assertEquals(BigDecimal("10000.00"), r.emi)
        assertEquals(BigDecimal.ZERO, r.totalInterest)
    }

    @Test fun `Amortization schedule length`() {
        val schedule = EmiCalculator.amortizationSchedule(
            principal = BigDecimal("100000"),
            annualRatePct = BigDecimal("10"),
            tenureMonths = 12,
        )
        assertEquals(12, schedule.size)
        // Final balance should be ~0
        assertTrue(schedule.last().balance.toDouble() < 1.0)
    }

    // ── SIP ───────────────────────────────────────────────────────────────────

    @Test fun `SIP 5000 at 12 percent for 10 years`() {
        val r = SipCalculator.calculate(
            monthlyAmount = BigDecimal("5000"),
            annualRatePct = BigDecimal("12"),
            years = 10,
        )
        // Expected ≈ 1,161,695
        val total = r.totalValue.toDouble()
        assertTrue("SIP total expected ≈1161695, got $total", total in 1150000.0..1175000.0)
        // Invested = 5000 * 120 = 600000
        assertEquals(BigDecimal("600000.00"), r.invested)
    }

    // ── Lump Sum ──────────────────────────────────────────────────────────────

    @Test fun `LumpSum 100000 at 10 percent for 5 years`() {
        val r = LumpSumCalculator.calculate(
            principal = BigDecimal("100000"),
            annualRatePct = BigDecimal("10"),
            years = 5,
        )
        // Expected 161051
        assertEquals(161051.0, r.totalValue.toDouble(), 1.0)
    }

    // ── FD ────────────────────────────────────────────────────────────────────

    @Test fun `FD 100000 at 7 percent for 5 years quarterly`() {
        val r = FdCalculator.calculate(
            principal = BigDecimal("100000"),
            annualRatePct = BigDecimal("7"),
            years = 5,
            frequency = CompoundingFrequency.Quarterly,
        )
        // Expected ≈ 141,478
        val total = r.totalValue.toDouble()
        assertTrue("FD total expected ≈141478, got $total", total in 141000.0..142000.0)
    }

    // ── Discount ──────────────────────────────────────────────────────────────

    @Test fun `Discount 1000 at 20 then extra 10`() {
        val r = DiscountCalculator.calculate(
            originalPrice = BigDecimal("1000"),
            discountPct = BigDecimal("20"),
            extraDiscountPct = BigDecimal("10"),
        )
        // 1000 * 0.8 = 800; 800 * 0.9 = 720
        assertEquals(BigDecimal("720.00"), r.finalPrice)
        assertEquals(BigDecimal("280.00"), r.youSave)
    }

    @Test fun `Discount reverse`() {
        val r = DiscountCalculator.reverse(
            finalPrice = BigDecimal("720"),
            discountPct = BigDecimal("20"),
        )
        // Original = 720 / 0.8 = 900
        assertEquals(BigDecimal("900.00").setScale(2), r.finalPrice.setScale(2))
    }
}
