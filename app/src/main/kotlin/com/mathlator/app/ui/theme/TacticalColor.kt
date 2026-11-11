package com.mathlator.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * "Neumorphic Tactical Dark" Hardware Design System Color Tokens.
 */
object TacticalColors {
    // Root app background
    val ObsidianChassis = Color(0xFF08090C)

    // Recessed OLED displays, input wells, telemetry cards
    val SurfaceRecessedWell = Color(0xFF0B0D12)
    val SurfaceRecessedWellActive = Color(0xFF0E1017)

    // Keycap Gradients
    // Numeric keys 0-9, ., 00
    val KeycapNumBase = Brush.linearGradient(
        listOf(Color(0xFF1A1E26), Color(0xFF12151B))
    )
    // Top utility row & converter side actions
    val KeycapUtilBase = Brush.linearGradient(
        listOf(Color(0xFF20242E), Color(0xFF151820))
    )
    // Math operators ÷, ×, −, + with warm carbon tint
    val KeycapOpBase = Brush.linearGradient(
        listOf(Color(0xFF251A17), Color(0xFF181210))
    )
    // Primary tactical orange action key
    val KeycapEqualsBase = Brush.linearGradient(
        listOf(Color(0xFFFF6A00), Color(0xFFE64800))
    )

    // Primary & Telemetry Accents
    val TacticalOrange = Color(0xFFFF5E00)
    val TacticalAmber = Color(0xFFFF9E00)
    val EmeraldTelemetry = Color(0xFF10B981)

    // Typography
    val TextPrimary = Color(0xFFF3F5F8)
    val TextSecondary = Color(0xFF8A94A6)
    val TextMuted = Color(0xFF4E5666)

    // Hardware Surfaces & Sockets
    val SurfaceCard = Color(0xFF12141A)
    val SurfaceCardGradient = Brush.linearGradient(
        listOf(Color(0xFF191C24), Color(0xFF0E1015))
    )
    val SurfaceDock = Color(0xFF0D0F14)
    val SurfaceSocket = Color(0xFF0A0C10)
    val OperatorOrangeGlow = Color(0x33FF5E00)
}
