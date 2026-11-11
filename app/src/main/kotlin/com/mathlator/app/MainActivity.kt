package com.mathlator.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
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

        // Initial transparent system bars before setContent
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        // Disable nav-bar contrast enforcement so the bar stays transparent even on 3-button nav (API 29+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val theme by settings.theme.collectAsState(initial = AppTheme.Dark)
            val isDark = when (theme) {
                AppTheme.Dark   -> true
                AppTheme.Light  -> false
                AppTheme.System -> isSystemDark()
            }

            // Update status/nav bar icon style dynamically based on theme:
            // Light icons (white) in Dark theme, dark icons in Light theme
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                )
                onDispose {}
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
