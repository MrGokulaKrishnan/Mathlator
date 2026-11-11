package com.mathlator.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** All app settings stored in DataStore Preferences. Changes are applied instantly. */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.dataStore

    // ── Keys ──────────────────────────────────────────────────────────────────
    private object Keys {
        val THEME           = stringPreferencesKey("theme")
        val HAPTICS         = booleanPreferencesKey("haptics")
        val KEY_SOUND       = booleanPreferencesKey("key_sound")
        val ANGLE_MODE      = stringPreferencesKey("angle_mode")
        val DECIMAL_PREC    = intPreferencesKey("decimal_precision")
        val DIGIT_GROUPING  = stringPreferencesKey("digit_grouping")
        val KEEP_SCREEN_ON  = booleanPreferencesKey("keep_screen_on")
        val ONE_HANDED      = booleanPreferencesKey("one_handed")
        val ONE_HANDED_SIDE = stringPreferencesKey("one_handed_side") // "left" | "right"
        val CURRENCY_FROM   = stringPreferencesKey("currency_from")
        val CURRENCY_TO     = stringPreferencesKey("currency_to")
        val CURRENCY_OUT2   = stringPreferencesKey("currency_out2")
    }

    // ── Defaults ──────────────────────────────────────────────────────────────
    companion object {
        fun getRegionDefaultCurrencies(): Triple<String, String, String> {
            return try {
                val locale = java.util.Locale.getDefault()
                val currency = java.util.Currency.getInstance(locale)
                val code = currency?.currencyCode ?: "INR"
                when (code) {
                    "USD" -> Triple("USD", "EUR", "GBP")
                    "EUR" -> Triple("EUR", "USD", "GBP")
                    else  -> Triple(code, "USD", "EUR")
                }
            } catch (_: Exception) {
                Triple("INR", "USD", "EUR")
            }
        }

        const val DEFAULT_DECIMAL_PREC   = 10
        val DEFAULT_GROUPING             = DigitGrouping.Indian
        val DEFAULT_ANGLE_MODE           = AngleMode.DEG
        val DEFAULT_THEME                = AppTheme.Dark
    }

    private val regionDefaults by lazy { getRegionDefaultCurrencies() }

    // ── Flows ─────────────────────────────────────────────────────────────────

    val theme: Flow<AppTheme> = store.data.map { p ->
        AppTheme.entries.find { it.name == p[Keys.THEME] } ?: DEFAULT_THEME
    }

    val haptics: Flow<Boolean> = store.data.map { p -> p[Keys.HAPTICS] ?: true }

    val keySound: Flow<Boolean> = store.data.map { p -> p[Keys.KEY_SOUND] ?: false }

    val angleMode: Flow<AngleMode> = store.data.map { p ->
        AngleMode.entries.find { it.name == p[Keys.ANGLE_MODE] } ?: DEFAULT_ANGLE_MODE
    }

    val decimalPrecision: Flow<Int> = store.data.map { p ->
        p[Keys.DECIMAL_PREC] ?: DEFAULT_DECIMAL_PREC
    }

    val digitGrouping: Flow<DigitGrouping> = store.data.map { p ->
        DigitGrouping.entries.find { it.name == p[Keys.DIGIT_GROUPING] } ?: DEFAULT_GROUPING
    }

    val keepScreenOn: Flow<Boolean> = store.data.map { p -> p[Keys.KEEP_SCREEN_ON] ?: false }

    val oneHandedEnabled: Flow<Boolean> = store.data.map { p -> p[Keys.ONE_HANDED] ?: false }

    val oneHandedSide: Flow<String> = store.data.map { p -> p[Keys.ONE_HANDED_SIDE] ?: "right" }

    val currencyFrom: Flow<String> = store.data.map { p ->
        p[Keys.CURRENCY_FROM] ?: regionDefaults.first
    }

    val currencyTo: Flow<String> = store.data.map { p ->
        p[Keys.CURRENCY_TO] ?: regionDefaults.second
    }

    val currencyOut2: Flow<String> = store.data.map { p ->
        p[Keys.CURRENCY_OUT2] ?: regionDefaults.third
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    suspend fun setTheme(theme: AppTheme) = store.edit { it[Keys.THEME] = theme.name }
    suspend fun setHaptics(on: Boolean) = store.edit { it[Keys.HAPTICS] = on }
    suspend fun setKeySound(on: Boolean) = store.edit { it[Keys.KEY_SOUND] = on }
    suspend fun setAngleMode(mode: AngleMode) = store.edit { it[Keys.ANGLE_MODE] = mode.name }
    suspend fun setDecimalPrecision(prec: Int) = store.edit { it[Keys.DECIMAL_PREC] = prec.coerceIn(2, 15) }
    suspend fun setDigitGrouping(g: DigitGrouping) = store.edit { it[Keys.DIGIT_GROUPING] = g.name }
    suspend fun setKeepScreenOn(on: Boolean) = store.edit { it[Keys.KEEP_SCREEN_ON] = on }
    suspend fun setOneHanded(on: Boolean) = store.edit { it[Keys.ONE_HANDED] = on }
    suspend fun setOneHandedSide(side: String) = store.edit { it[Keys.ONE_HANDED_SIDE] = side }
    suspend fun setCurrencyFrom(code: String) = store.edit { it[Keys.CURRENCY_FROM] = code }
    suspend fun setCurrencyTo(code: String) = store.edit { it[Keys.CURRENCY_TO] = code }
    suspend fun setCurrencyOut2(code: String) = store.edit { it[Keys.CURRENCY_OUT2] = code }
}
