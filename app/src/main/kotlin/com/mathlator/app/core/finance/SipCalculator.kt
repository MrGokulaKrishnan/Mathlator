package com.mathlator.app.core.finance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext.DECIMAL128
private val HALF_UP = RoundingMode.HALF_UP
private val BD100 = BigDecimal("100")
private val BD1200 = BigDecimal("1200")
private val BD12 = BigDecimal("12")

// ── SIP Calculator ─────────────────────────────────────────────────────────────

/**
 * Result of an investment (SIP, lump sum, FD) calculation.
 *
 * @param invested       Total amount invested.
 * @param estimatedReturn Estimated returns.
 * @param totalValue     Final corpus.
 * @param yearlyValues   (year → total value) for charting.
 */
data class InvestmentResult(
    val invested: BigDecimal,
    val estimatedReturn: BigDecimal,
    val totalValue: BigDecimal,
    val yearlyValues: List<Pair<Int, BigDecimal>> = emptyList(),
)

/** Calculates SIP (Systematic Investment Plan) projections. */
object SipCalculator {

    /**
     * Calculates SIP future value.
     *
     * Without step-up:
     * `FV = P × ((1+i)^n − 1) / i × (1+i)`
     *
     * With step-up: simulates month by month, increasing P by [stepUpPct] every 12 months.
     *
     * @param monthlyAmount Monthly SIP installment.
     * @param annualRatePct Expected return % per annum.
     * @param years         Investment horizon in years.
     * @param stepUpPct     Annual step-up percentage (0 = no step-up).
     */
    fun calculate(
        monthlyAmount: BigDecimal,
        annualRatePct: BigDecimal,
        years: Int,
        stepUpPct: BigDecimal = BigDecimal.ZERO,
    ): InvestmentResult {
        val n = years * 12
        val i = annualRatePct.divide(BD1200, MC)

        if (stepUpPct.compareTo(BigDecimal.ZERO) == 0) {
            // Simple SIP formula
            val onePlusI = BigDecimal.ONE.add(i, MC)
            val pow = onePlusI.pow(n, MC)
            val fv = monthlyAmount.multiply(pow.subtract(BigDecimal.ONE, MC), MC)
                .divide(i, MC)
                .multiply(onePlusI, MC)
            val invested = monthlyAmount.multiply(BigDecimal(n), MC)
            val returns = fv.subtract(invested, MC)

            val yearly = buildYearlySip(monthlyAmount, i, years)

            return InvestmentResult(
                invested        = invested.setScale(2, HALF_UP),
                estimatedReturn = returns.setScale(2, HALF_UP),
                totalValue      = fv.setScale(2, HALF_UP),
                yearlyValues    = yearly,
            )
        } else {
            // Step-up simulation
            var corpus = BigDecimal.ZERO
            var invested = BigDecimal.ZERO
            var monthly = monthlyAmount
            val stepFactor = BigDecimal.ONE.add(stepUpPct.divide(BD100, MC), MC)
            val yearlyValues = mutableListOf<Pair<Int, BigDecimal>>()

            for (month in 1..n) {
                if (month > 1 && (month - 1) % 12 == 0) {
                    monthly = monthly.multiply(stepFactor, MC)
                }
                corpus = corpus.multiply(BigDecimal.ONE.add(i, MC), MC).add(monthly, MC)
                invested = invested.add(monthly, MC)
                if (month % 12 == 0) {
                    yearlyValues.add(month / 12 to corpus.setScale(2, HALF_UP))
                }
            }

            return InvestmentResult(
                invested        = invested.setScale(2, HALF_UP),
                estimatedReturn = corpus.subtract(invested, MC).setScale(2, HALF_UP),
                totalValue      = corpus.setScale(2, HALF_UP),
                yearlyValues    = yearlyValues,
            )
        }
    }

    private fun buildYearlySip(
        monthly: BigDecimal,
        monthlyRate: BigDecimal,
        years: Int,
    ): List<Pair<Int, BigDecimal>> {
        val onePlusI = BigDecimal.ONE.add(monthlyRate, MC)
        return (1..years).map { y ->
            val n = y * 12
            val pow = onePlusI.pow(n, MC)
            val fv = monthly.multiply(pow.subtract(BigDecimal.ONE, MC), MC)
                .divide(monthlyRate, MC)
                .multiply(onePlusI, MC)
            y to fv.setScale(2, HALF_UP)
        }
    }
}

// ── Lump Sum Calculator ────────────────────────────────────────────────────────

/** Calculates lump sum investment projections. */
object LumpSumCalculator {

    /**
     * FV = P × (1 + r/100)^t
     *
     * @param principal     Initial investment.
     * @param annualRatePct Expected return % per annum.
     * @param years         Investment horizon in years.
     */
    fun calculate(
        principal: BigDecimal,
        annualRatePct: BigDecimal,
        years: Int,
    ): InvestmentResult {
        val factor = BigDecimal.ONE.add(annualRatePct.divide(BD100, MC), MC)
        val fv = principal.multiply(factor.pow(years, MC), MC)
        val returns = fv.subtract(principal, MC)

        val yearly = (1..years).map { y ->
            y to principal.multiply(factor.pow(y, MC), MC).setScale(2, HALF_UP)
        }

        return InvestmentResult(
            invested        = principal.setScale(2, HALF_UP),
            estimatedReturn = returns.setScale(2, HALF_UP),
            totalValue      = fv.setScale(2, HALF_UP),
            yearlyValues    = yearly,
        )
    }
}

// ── FD Calculator ─────────────────────────────────────────────────────────────

/** Compounding frequency for Fixed Deposits. */
enum class CompoundingFrequency(val timesPerYear: Int, val labelResKey: String) {
    Monthly(12, "investment_compound_monthly"),
    Quarterly(4, "investment_compound_quarterly"),
    HalfYearly(2, "investment_compound_halfyearly"),
    Yearly(1, "investment_compound_yearly"),
}

/** Calculates Fixed Deposit maturity and returns. */
object FdCalculator {

    /**
     * FD formula: `A = P × (1 + r/(100n))^(n×t)`
     *
     * Also computes effective annual yield.
     *
     * @param principal     Principal amount.
     * @param annualRatePct Annual interest rate in percent.
     * @param years         Tenure in years.
     * @param frequency     Compounding frequency.
     */
    fun calculate(
        principal: BigDecimal,
        annualRatePct: BigDecimal,
        years: Int,
        frequency: CompoundingFrequency = CompoundingFrequency.Quarterly,
    ): InvestmentResult {
        val n = BigDecimal(frequency.timesPerYear)
        val t = BigDecimal(years)
        val rOver100n = annualRatePct.divide(BD100.multiply(n, MC), MC)
        val base = BigDecimal.ONE.add(rOver100n, MC)
        val exp = frequency.timesPerYear * years
        val maturity = principal.multiply(base.pow(exp, MC), MC)
        val returns = maturity.subtract(principal, MC)

        val yearly = (1..years).map { y ->
            val e = frequency.timesPerYear * y
            y to principal.multiply(base.pow(e, MC), MC).setScale(2, HALF_UP)
        }

        return InvestmentResult(
            invested        = principal.setScale(2, HALF_UP),
            estimatedReturn = returns.setScale(2, HALF_UP),
            totalValue      = maturity.setScale(2, HALF_UP),
            yearlyValues    = yearly,
        )
    }
}
