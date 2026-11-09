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

@Composable
fun MathlatorLogo(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    val barBrush = brandGradientBrush()
    val barWidth = size * 5.8f
    val barHeight = size

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
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }

        // "MATHLATOR" wordmark
        Text(
            text = "MATHLATOR",
            style = WordmarkTextStyle.copy(fontSize = (size.value * 0.8f).sp),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = size * 0.5f),
        )

        // Bottom bar
        Canvas(modifier = Modifier.size(barWidth, barHeight)) {
            drawRoundRect(
                brush = barBrush,
                size = this.size,
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }
    }
}
