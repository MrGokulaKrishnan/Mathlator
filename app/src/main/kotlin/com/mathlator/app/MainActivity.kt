package com.mathlator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.ui.navigation.MathlatorNavHost
import com.mathlator.app.ui.theme.AppTheme
import com.mathlator.app.ui.theme.MathlatorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by settings.theme.collectAsState(initial = AppTheme.Dark)
            val isDark = when (theme) {
                AppTheme.Dark   -> true
                AppTheme.Light  -> false
                AppTheme.System -> isSystemDark()
            }
            MathlatorTheme(isDark = isDark) {
                MathlatorNavHost(settingsRepository = settings)
            }
        }
    }

    private fun isSystemDark(): Boolean {
        val uiMode = resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
