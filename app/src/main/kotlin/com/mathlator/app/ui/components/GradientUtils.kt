package com.mathlator.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.mathlator.app.ui.theme.BrandEnd
import com.mathlator.app.ui.theme.BrandStart

/** Shared brand gradient brush (left → right). */
fun brandGradientBrush() = Brush.linearGradient(
    colors = listOf(BrandStart, BrandEnd),
    start = Offset.Zero,
    end = Offset(Float.MAX_VALUE, 0f),
)
