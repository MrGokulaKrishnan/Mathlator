package com.mathlator.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.WordmarkTextStyle

/**
 * Reusable Mathlator brand logo: two horizontal orange-gradient bars with the
 * white wordmark MATHLATOR centered between them.
 */
@Composable
fun MathlatorLogo(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    val barBrush = brandGradientBrush()
    val barWidth = size * 6.0f
    val barHeight = size * 1.05f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Top bar
        Canvas(modifier = Modifier.size(barWidth, barHeight)) {
            drawRoundRect(
                brush = barBrush,
                size = this.size,
                cornerRadius = CornerRadius(barHeight.toPx() * 0.28f),
            )
        }

        // "MATHLATOR" wordmark
        Text(
            text = "MATHLATOR",
            style = WordmarkTextStyle.copy(fontSize = (size.value * 0.88f).sp),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = size * 0.45f),
        )

        // Bottom bar
        Canvas(modifier = Modifier.size(barWidth, barHeight)) {
            drawRoundRect(
                brush = barBrush,
                size = this.size,
                cornerRadius = CornerRadius(barHeight.toPx() * 0.28f),
            )
        }
    }
}
