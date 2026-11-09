package com.mathlator.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.data.db.HistoryDao
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.data.rates.RatesRepository
import com.mathlator.app.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val historyDao: HistoryDao,
    private val ratesRepository: RatesRepository,
) : ViewModel() {

    val theme: StateFlow<AppTheme> = settings.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.Dark)

    val haptics: StateFlow<Boolean> = settings.haptics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val keySound: StateFlow<Boolean> = settings.keySound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val angleMode: StateFlow<AngleMode> = settings.angleMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AngleMode.DEG)

    val decimalPrecision: StateFlow<Int> = settings.decimalPrecision
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val digitGrouping: StateFlow<DigitGrouping> = settings.digitGrouping
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DigitGrouping.Indian)

    val keepScreenOn: StateFlow<Boolean> = settings.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val oneHandedSide: StateFlow<String> = settings.oneHandedSide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "right")

    val currencyFrom: StateFlow<String> = settings.currencyFrom
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "INR")

    val currencyTo: StateFlow<String> = settings.currencyTo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    fun setTheme(theme: AppTheme) = viewModelScope.launch { settings.setTheme(theme) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settings.setHaptics(enabled) }
    fun setKeySound(enabled: Boolean) = viewModelScope.launch { settings.setKeySound(enabled) }
    fun setAngleMode(mode: AngleMode) = viewModelScope.launch { settings.setAngleMode(mode) }
    fun setDecimalPrecision(precision: Int) = viewModelScope.launch { settings.setDecimalPrecision(precision) }
    fun setDigitGrouping(grouping: DigitGrouping) = viewModelScope.launch { settings.setDigitGrouping(grouping) }
    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch { settings.setKeepScreenOn(enabled) }
    fun setOneHandedSide(side: String) = viewModelScope.launch { settings.setOneHandedSide(side) }

    fun clearHistory(onCleared: () -> Unit) = viewModelScope.launch {
        historyDao.deleteAll()
        onCleared()
    }

    fun clearCachedRates(onCleared: () -> Unit) = viewModelScope.launch {
        ratesRepository.clearCache()
        onCleared()
    }
}
