package com.mathlator.app.core.finance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext.DECIMAL128
private val HALF_UP = RoundingMode.HALF_UP
private val BD100 = BigDecimal("100")

// ── GST Calculator ─────────────────────────────────────────────────────────────

/**
 * Result of a GST calculation.
 *
 * @param baseAmount  Pre-GST amount.
 * @param gstAmount   GST portion.
 * @param totalAmount Total with GST.
 * @param cgst        Central GST (gstAmount / 2) for intra-state.
 * @param sgst        State GST (gstAmount / 2) for intra-state.
 */
data class GstResult(
    val baseAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val cgst: BigDecimal,
    val sgst: BigDecimal,
)

/** Performs GST add and remove calculations. */
object GstCalculator {

    /**
     * Add GST: given a pre-tax [amount] and [ratePercent], compute the GST amount and total.
     */
    fun addGst(amount: BigDecimal, ratePercent: BigDecimal): GstResult {
        val gst = amount.multiply(ratePercent, MC).divide(BD100, 2, HALF_UP)
        val total = amount.add(gst, MC).setScale(2, HALF_UP)
        val half = gst.divide(BigDecimal("2"), 2, HALF_UP)
        return GstResult(
            baseAmount  = amount.setScale(2, HALF_UP),
            gstAmount   = gst,
            totalAmount = total,
            cgst = half,
            sgst = half,
        )
    }

    /**
     * Remove GST: given a GST-inclusive [amount] and [ratePercent], recover the base price.
     */
    fun removeGst(amount: BigDecimal, ratePercent: BigDecimal): GstResult {
        val factor = BigDecimal.ONE.add(ratePercent.divide(BD100, MC), MC)
        val base = amount.divide(factor, 10, HALF_UP).setScale(2, HALF_UP)
        val gst = amount.subtract(base, MC).setScale(2, HALF_UP)
        val total = amount.setScale(2, HALF_UP)
        val half = gst.divide(BigDecimal("2"), 2, HALF_UP)
        return GstResult(
            baseAmount  = base,
            gstAmount   = gst,
            totalAmount = total,
            cgst = half,
            sgst = half,
        )
    }
}
