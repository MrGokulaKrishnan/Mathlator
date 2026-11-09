package com.mathlator.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// ── Font families ─────────────────────────────────────────────────────────────
// Per Spec Section 3: "Typography: Montserrat, Inter ... fall back to FontFamily.SansSerif if a download is impossible"
val MontserratFamily: FontFamily = FontFamily.SansSerif
val InterFamily: FontFamily = FontFamily.SansSerif

// ── Tabular-figures helper ────────────────────────────────────────────────────
/** TextStyle that forces tabular figures in Inter / SansSerif. */
fun interTabular(
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    fontFeatureSettings = "tnum",
)

// ── Scale ─────────────────────────────────────────────────────────────────────

/** 64sp Light — calculator result. */
val ResultTextStyle = interTabular(FontWeight.Light, 64.sp)

/** 32sp Light — expression line. */
val ExpressionTextStyle = interTabular(FontWeight.Light, 32.sp)

/** 32sp Light — standard keys. */
val KeyTextStyle = interTabular(FontWeight.Light, 32.sp)

/** 20sp — scientific keys. */
val SciKeyTextStyle = interTabular(FontWeight.Normal, 20.sp)

/** 20sp Medium — screen titles (Montserrat). */
val TitleTextStyle = TextStyle(
    fontFamily = MontserratFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
)

/** 16sp — body. */
val BodyTextStyle = interTabular(FontWeight.Normal, 16.sp)

/** 14sp — label. */
val LabelTextStyle = interTabular(FontWeight.Normal, 14.sp)

/** 16sp Regular — grid labels. */
val GridLabelTextStyle = interTabular(FontWeight.Normal, 16.sp)

/** Montserrat wordmark — letter-spacing 0.18em. */
val WordmarkTextStyle = TextStyle(
    fontFamily = MontserratFamily,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    letterSpacing = 0.18.em,
    textAlign = TextAlign.Center,
)

// ── Material3 Typography ──────────────────────────────────────────────────────

val MathlatorTypography = Typography(
    displayLarge   = interTabular(FontWeight.Light, 57.sp),
    displayMedium  = interTabular(FontWeight.Light, 45.sp),
    displaySmall   = interTabular(FontWeight.Light, 36.sp),
    headlineLarge  = TitleTextStyle.copy(fontSize = 32.sp),
    headlineMedium = TitleTextStyle.copy(fontSize = 28.sp),
    headlineSmall  = TitleTextStyle,
    titleLarge     = TitleTextStyle,
    titleMedium    = interTabular(FontWeight.Medium, 16.sp),
    titleSmall     = interTabular(FontWeight.Medium, 14.sp),
    bodyLarge      = BodyTextStyle,
    bodyMedium     = interTabular(FontWeight.Normal, 14.sp),
    bodySmall      = LabelTextStyle,
    labelLarge     = interTabular(FontWeight.Medium, 14.sp),
    labelMedium    = LabelTextStyle,
    labelSmall     = interTabular(FontWeight.Normal, 11.sp),
)

private val Number.em: androidx.compose.ui.unit.TextUnit
    get() = this.toFloat().em

private val Float.em: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Em)
