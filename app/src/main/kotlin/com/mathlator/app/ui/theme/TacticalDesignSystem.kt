package com.mathlator.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design system dimensions, typography tokens, and elevation constants
 * for the "Neumorphic Tactical Dark" hardware design system.
 */
object TacticalDimens {
    val KeycapCornerRadius = 20.dp
    val WellCornerRadius = 22.dp
    val DisplayWellCornerRadius = 24.dp
    val CardCornerRadius = 20.dp
    val BentoCardSpacing = 12.dp
    val KeypadSpacing = 10.dp
    val KeypadKeyHeight = 62.dp
    val DockCornerRadius = 16.dp
    val SocketRadius = 14.dp
}

object TacticalTypography {
    val TelemetryMicroBadge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = TacticalColors.TacticalOrange,
    )

    val OledReadoutLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        letterSpacing = (-0.5).sp,
        color = TacticalColors.TextPrimary,
    )

    val OledReadoutMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
        color = TacticalColors.TextPrimary,
    )

    val BentoTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = TacticalColors.TextPrimary,
    )

    val BentoSubtitle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = TacticalColors.TextSecondary,
    )

    val TelemetryValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TacticalColors.TextPrimary,
    )

    val TelemetryLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = TacticalColors.TextSecondary,
    )

    val KeycapNumLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = TacticalColors.TextPrimary,
    )

    val KeycapUtilLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        color = TacticalColors.TacticalOrange,
    )
}
