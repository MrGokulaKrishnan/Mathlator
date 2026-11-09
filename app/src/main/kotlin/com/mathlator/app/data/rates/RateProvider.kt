package com.mathlator.app.data.rates

import java.math.BigDecimal

/** Exchange rate response from a provider. */
data class RateResponse(
    val base: String,
    val rates: Map<String, BigDecimal>,
    val fetchedAt: Long = System.currentTimeMillis(),
)

/** Provider result — Success or typed error. */
sealed class RateProviderResult {
    data class Success(val response: RateResponse) : RateProviderResult()
    object NetworkError : RateProviderResult()
    object ParseError : RateProviderResult()
}

/** Interface for exchange rate providers. */
interface RateProvider {
    /** Fetches rates for the given [baseCurrency] (3-letter ISO code, uppercase). */
    suspend fun fetchRates(baseCurrency: String): RateProviderResult
}
