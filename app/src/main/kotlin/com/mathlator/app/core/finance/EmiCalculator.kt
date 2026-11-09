package com.mathlator.app.core.finance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext.DECIMAL128
private val HALF_UP = RoundingMode.HALF_UP
private val BD100 = BigDecimal("100")
private val BD1200 = BigDecimal("1200")

// ── EMI Calculator ─────────────────────────────────────────────────────────────

/**
 * Result of an EMI calculation.
 *
 * @param emi           Monthly EMI payment.
 * @param totalInterest Total interest paid.
 * @param totalPayment  Total amount repaid (principal + interest).
 */
data class EmiResult(
    val emi: BigDecimal,
    val totalInterest: BigDecimal,
    val totalPayment: BigDecimal,
)

/** Monthly amortization entry. */
data class AmortizationEntry(
    val month: Int,
    val emi: BigDecimal,
    val principal: BigDecimal,
    val interest: BigDecimal,
    val balance: BigDecimal,
)

/** Computes EMI and amortization schedule using [BigDecimal]. */
object EmiCalculator {

    /**
     * Calculates monthly EMI.
     *
     * Formula: `EMI = P × i × (1+i)^n / ((1+i)^n − 1)`.
     * When rate is 0: `EMI = P / n`.
     *
     * @param principal     Loan principal in any currency.
     * @param annualRatePct Annual interest rate in percent (e.g., 9 for 9%).
     * @param tenureMonths  Loan tenure in months.
     */
    fun calculate(
        principal: BigDecimal,
        annualRatePct: BigDecimal,
        tenureMonths: Int,
    ): EmiResult {
        require(principal > BigDecimal.ZERO) { "Principal must be positive" }
        require(tenureMonths > 0) { "Tenure must be positive" }

        val n = BigDecimal(tenureMonths)

        if (annualRatePct.compareTo(BigDecimal.ZERO) == 0) {
            val emi = principal.divide(n, 2, HALF_UP)
            return EmiResult(
                emi           = emi,
                totalInterest = BigDecimal.ZERO,
                totalPayment  = principal.setScale(2, HALF_UP),
            )
        }

        val i = annualRatePct.divide(BD1200, MC)   // monthly rate
        val onePlusI = BigDecimal.ONE.add(i, MC)
        val pow = onePlusI.pow(tenureMonths, MC)
        val numerator = principal.multiply(i, MC).multiply(pow, MC)
        val denominator = pow.subtract(BigDecimal.ONE, MC)
        val emi = numerator.divide(denominator, 2, HALF_UP)
        val totalPayment = emi.multiply(n, MC).setScale(2, HALF_UP)
        val totalInterest = totalPayment.subtract(principal, MC).setScale(2, HALF_UP)

        return EmiResult(
            emi           = emi,
            totalInterest = totalInterest,
            totalPayment  = totalPayment,
        )
    }

    /**
     * Builds the full month-by-month amortization schedule.
     */
    fun amortizationSchedule(
        principal: BigDecimal,
        annualRatePct: BigDecimal,
        tenureMonths: Int,
    ): List<AmortizationEntry> {
        val result = calculate(principal, annualRatePct, tenureMonths)
        val emi = result.emi
        val monthlyRate = annualRatePct.divide(BD1200, MC)
        val schedule = mutableListOf<AmortizationEntry>()
        var balance = principal.setScale(10, HALF_UP)

        for (month in 1..tenureMonths) {
            val interestPart = balance.multiply(monthlyRate, MC).setScale(2, HALF_UP)
            val principalPart = if (month == tenureMonths) {
                balance.setScale(2, HALF_UP)
            } else {
                emi.subtract(interestPart, MC).setScale(2, HALF_UP)
            }
            balance = balance.subtract(principalPart, MC).coerceAtLeast(BigDecimal.ZERO)

            schedule.add(
                AmortizationEntry(
                    month     = month,
                    emi       = emi,
                    principal = principalPart,
                    interest  = interestPart,
                    balance   = balance.setScale(2, HALF_UP),
                )
            )
        }
        return schedule
    }

    private fun BigDecimal.coerceAtLeast(min: BigDecimal): BigDecimal =
        if (this < min) min else this
}
