package com.mathlator.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── App theme setting ─────────────────────────────────────────────────────────

enum class AppTheme { Dark, Light, System }

// ── Composition locals for custom color tokens ─────────────────────────────────

data class MathlatorColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val divider: Color,
    val operator: Color,
    val textMain: Color,
    val textDim: Color,
    val error: Color,
    val primary: Color = Primary,
    val brandStart: Color = BrandStart,
    val brandEnd: Color = BrandEnd,
    val donutSecondary: Color = DonutSecondary,
    val isDark: Boolean,
)

val LocalMathlatorColors = staticCompositionLocalOf {
    darkMathlatorColors()
}

fun darkMathlatorColors() = MathlatorColors(
    background  = TacticalColors.ObsidianChassis,
    surface     = TacticalColors.SurfaceRecessedWell,
    surfaceHigh = TacticalColors.SurfaceCard,
    divider     = Color(0x14FFFFFF),
    operator    = TacticalColors.TacticalOrange,
    textMain    = TacticalColors.TextPrimary,
    textDim     = TacticalColors.TextSecondary,
    error       = ErrorDark,
    primary     = TacticalColors.TacticalOrange,
    brandStart  = TacticalColors.TacticalOrange,
    brandEnd    = TacticalColors.TacticalAmber,
    isDark      = true,
)

fun lightMathlatorColors() = MathlatorColors(
    background  = BackgroundLight,
    surface     = SurfaceLight,
    surfaceHigh = SurfaceHighLight,
    divider     = DividerLight,
    operator    = OperatorLight,
    textMain    = TextMainLight,
    textDim     = TextDimLight,
    error       = ErrorLight,
    isDark      = false,
)

// ── Material color schemes (minimal — we mostly use our custom tokens) ─────────

private val DarkColorScheme = darkColorScheme(
    primary           = Primary,
    background        = BackgroundDark,
    surface           = SurfaceDark,
    onPrimary         = Color.White,
    onBackground      = TextMainDark,
    onSurface         = TextMainDark,
    error             = ErrorDark,
    onError           = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary           = Primary,
    background        = BackgroundLight,
    surface           = SurfaceLight,
    onPrimary         = Color.White,
    onBackground      = TextMainLight,
    onSurface         = TextMainLight,
    error             = ErrorLight,
    onError           = Color.White,
)

// ── Main theme composable ─────────────────────────────────────────────────────

@Composable
fun MathlatorTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit,
) {
    val mathlatorColors = if (isDark) darkMathlatorColors() else lightMathlatorColors()
    val materialColorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalMathlatorColors provides mathlatorColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography  = MathlatorTypography,
            shapes      = MathlatorShapes,
            content     = content,
        )
    }
}

/** Convenience accessor. */
val mathlatorColors: MathlatorColors
    @Composable get() = LocalMathlatorColors.current
