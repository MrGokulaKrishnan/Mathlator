package com.mathlator.app.data.rates

import com.mathlator.app.data.db.RateDao
import com.mathlator.app.data.db.RateSnapshotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/** Cache TTL in milliseconds (12 hours). */
private const val CACHE_TTL_MS = 12 * 60 * 60 * 1000L

/** Current state of the rates repository. */
sealed class RatesState {
    /** Rates loaded successfully, with optional offline flag. */
    data class Ready(
        val rates: Map<String, BigDecimal>,
        val base: String,
        val fetchedAt: Long,
        val isOffline: Boolean = false,
        val usedFallback: Boolean = false,
    ) : RatesState()

    /** No data available (no network + no cache). */
    object NoData : RatesState()

    /** Currently loading. */
    object Loading : RatesState()

    /** Error state. */
    data class Error(val message: String) : RatesState()
}

/**
 * Manages currency exchange rates with 12-hour cache and automatic fallback.
 *
 * Strategy:
 * 1. Serve cache if younger than 12h.
 * 2. Otherwise fetch from primary (OpenErApi), falling back to Frankfurter.
 * 3. On network failure, serve stale cache with [RatesState.Ready.isOffline] = true.
 * 4. No cache + no network → [RatesState.NoData].
 *
 * Retry: exponential backoff, max 2 retries.
 */
@Singleton
class RatesRepository @Inject constructor(
    private val rateDao: RateDao,
    private val primary: OpenErApiProvider,
    private val fallback: FrankfurterProvider,
) {

    fun observeRates(base: String): Flow<RatesState> =
        rateDao.observeRates(base).map { entities ->
            if (entities.isEmpty()) RatesState.NoData
            else {
                val rates = entities.associate { it.code to BigDecimal(it.rate) }
                val fetchedAt = entities.minOf { it.fetchedAt }
                RatesState.Ready(rates, base, fetchedAt, isOffline = true)
            }
        }

    /**
     * Refreshes rates for [base] currency.
     * Uses cached data if younger than [CACHE_TTL_MS], unless [force] is true.
     *
     * @return [RatesState.Ready] on success; [RatesState.NoData] if offline with no cache.
     */
    suspend fun refresh(base: String, force: Boolean = false): RatesState =
        withContext(Dispatchers.IO) {
            val cachedAt = rateDao.oldestFetchedAt(base)
            val cacheAge = if (cachedAt != null) System.currentTimeMillis() - cachedAt else Long.MAX_VALUE

            if (!force && cacheAge < CACHE_TTL_MS) {
                val cached = rateDao.getRates(base)
                if (cached.isNotEmpty()) {
                    val rates = cached.associate { it.code to BigDecimal(it.rate) }
                    return@withContext RatesState.Ready(rates, base, cachedAt!!, isOffline = false)
                }
            }

            // Try network with retries
            val networkResult = fetchWithRetry(base)
            when (networkResult) {
                is RateProviderResult.Success -> {
                    val response = networkResult.response
                    val now = System.currentTimeMillis()
                    val entities = response.rates.map { (code, rate) ->
                        RateSnapshotEntity(base, code, rate.toPlainString(), now)
                    }
                    rateDao.deleteByBase(base)
                    rateDao.insertAll(entities)
                    RatesState.Ready(response.rates, base, now, isOffline = false)
                }
                else -> {
                    // Fall back to cache
                    val cached = rateDao.getRates(base)
                    if (cached.isNotEmpty()) {
                        val rates = cached.associate { it.code to BigDecimal(it.rate) }
                        RatesState.Ready(rates, base, cachedAt ?: 0L, isOffline = true)
                    } else {
                        RatesState.NoData
                    }
                }
            }
        }

    private suspend fun fetchWithRetry(base: String): RateProviderResult {
        var delay = 1000L
        repeat(3) { attempt ->
            val result = primary.fetchRates(base)
            if (result is RateProviderResult.Success) return result
            if (attempt < 2) {
                delay(delay)
                delay *= 2
            }
        }
        // Try fallback
        return fallback.fetchRates(base)
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        rateDao.deleteAll()
    }
}
