package com.mathlator.app.core.finance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext.DECIMAL128
private val HALF_UP = RoundingMode.HALF_UP
private val BD100 = BigDecimal("100")

/**
 * Result of a discount calculation.
 *
 * @param finalPrice       Final price after all discounts and tax.
 * @param youSave          Amount saved from the original price.
 * @param effectiveDiscount Effective discount percentage.
 */
data class DiscountResult(
    val finalPrice: BigDecimal,
    val youSave: BigDecimal,
    val effectiveDiscount: BigDecimal,
)

/** Calculates discounts (single or stacked) with optional tax. */
object DiscountCalculator {

    /**
     * Calculates forward discount: original → discounted → with tax.
     *
     * @param originalPrice   The starting price.
     * @param discountPct     First discount percentage.
     * @param extraDiscountPct Optional second discount applied after the first (default 0).
     * @param taxPct           Optional tax percentage applied to the discounted price (default 0).
     */
    fun calculate(
        originalPrice: BigDecimal,
        discountPct: BigDecimal,
        extraDiscountPct: BigDecimal = BigDecimal.ZERO,
        taxPct: BigDecimal = BigDecimal.ZERO,
    ): DiscountResult {
        val afterFirst = originalPrice.multiply(
            BigDecimal.ONE.subtract(discountPct.divide(BD100, MC), MC), MC
        )
        val afterExtra = if (extraDiscountPct.compareTo(BigDecimal.ZERO) != 0) {
            afterFirst.multiply(
                BigDecimal.ONE.subtract(extraDiscountPct.divide(BD100, MC), MC), MC
            )
        } else afterFirst

        val finalPrice = if (taxPct.compareTo(BigDecimal.ZERO) != 0) {
            afterExtra.multiply(BigDecimal.ONE.add(taxPct.divide(BD100, MC), MC), MC)
        } else afterExtra

        val youSave = originalPrice.subtract(finalPrice, MC).coerceAtLeast(BigDecimal.ZERO)
        val effectiveDiscount = youSave.divide(originalPrice, MC).multiply(BD100, MC)

        return DiscountResult(
            finalPrice       = finalPrice.setScale(2, HALF_UP),
            youSave          = youSave.setScale(2, HALF_UP),
            effectiveDiscount = effectiveDiscount.setScale(2, HALF_UP),
        )
    }

    /**
     * Reverse calculation: find original price given [finalPrice] and [discountPct].
     *
     * @param finalPrice  The price after discount (and optional tax).
     * @param discountPct The discount percentage that was applied.
     * @param taxPct      Tax percentage that was applied to the discounted price.
     */
    fun reverse(
        finalPrice: BigDecimal,
        discountPct: BigDecimal,
        taxPct: BigDecimal = BigDecimal.ZERO,
    ): DiscountResult {
        // Remove tax first
        val preTax = if (taxPct.compareTo(BigDecimal.ZERO) != 0) {
            finalPrice.divide(BigDecimal.ONE.add(taxPct.divide(BD100, MC), MC), MC)
        } else finalPrice

        // Remove discount
        val original = preTax.divide(
            BigDecimal.ONE.subtract(discountPct.divide(BD100, MC), MC), MC
        )

        val youSave = original.subtract(finalPrice, MC).coerceAtLeast(BigDecimal.ZERO)
        val effectiveDiscount = youSave.divide(original, MC).multiply(BD100, MC)

        return DiscountResult(
            finalPrice        = original.setScale(2, HALF_UP),
            youSave           = youSave.setScale(2, HALF_UP),
            effectiveDiscount = effectiveDiscount.setScale(2, HALF_UP),
        )
    }

    private fun BigDecimal.coerceAtLeast(min: BigDecimal): BigDecimal =
        if (this < min) min else this
}
