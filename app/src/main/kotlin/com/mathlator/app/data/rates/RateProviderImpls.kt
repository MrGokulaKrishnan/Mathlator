package com.mathlator.app.data.rates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * Primary exchange rate provider using open.er-api.com (no API key required).
 * Provides ~160 currencies.
 * No attribution required by their terms for non-commercial use.
 */
class OpenErApiProvider(private val client: OkHttpClient) : RateProvider {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class ErApiResponse(
        val result: String = "",
        @SerialName("base_code") val baseCode: String = "",
        val rates: Map<String, Double> = emptyMap(),
    )

    override suspend fun fetchRates(baseCurrency: String): RateProviderResult {
        return try {
            val url = "https://open.er-api.com/v6/latest/$baseCurrency"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return RateProviderResult.NetworkError

            val body = response.body?.string() ?: return RateProviderResult.ParseError
            val parsed = json.decodeFromString<ErApiResponse>(body)
            if (parsed.result != "success") return RateProviderResult.NetworkError

            val rates = parsed.rates.mapValues { (_, v) ->
                BigDecimal(v.toString())
            }
            RateProviderResult.Success(
                RateResponse(
                    base = parsed.baseCode,
                    rates = rates,
                )
            )
        } catch (e: Exception) {
            RateProviderResult.NetworkError
        }
    }
}

/**
 * Fallback exchange rate provider using api.frankfurter.dev (ECB rates, ~33 currencies).
 * Attribution: "Rates from Frankfurter (api.frankfurter.dev)" shown in footer.
 */
class FrankfurterProvider(private val client: OkHttpClient) : RateProvider {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class FrankfurterResponse(
        val base: String = "",
        val rates: Map<String, Double> = emptyMap(),
    )

    override suspend fun fetchRates(baseCurrency: String): RateProviderResult {
        return try {
            val url = "https://api.frankfurter.dev/v1/latest?base=$baseCurrency"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return RateProviderResult.NetworkError

            val body = response.body?.string() ?: return RateProviderResult.ParseError
            val parsed = json.decodeFromString<FrankfurterResponse>(body)

            // Frankfurter doesn't include the base in its rates; add it at 1.0
            val rates = (parsed.rates.mapValues { (_, v) -> BigDecimal(v.toString()) } +
                    mapOf(baseCurrency to BigDecimal.ONE))

            RateProviderResult.Success(
                RateResponse(base = parsed.base, rates = rates)
            )
        } catch (e: Exception) {
            RateProviderResult.NetworkError
        }
    }
}
