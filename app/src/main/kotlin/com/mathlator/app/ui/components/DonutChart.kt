package com.mathlator.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.DonutSecondary
import com.mathlator.app.ui.theme.mathlatorColors

/**
 * Donut chart visualizing Principal (brand gradient) vs Interest (#3A3A3C).
 */
@Composable
fun DonutChart(
    principalRatio: Float, // 0.0 to 1.0
    interestRatio: Float,  // 0.0 to 1.0
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 22.dp,
    centerTitle: String = "",
    centerSubtitle: String = "",
) {
    val colors = mathlatorColors
    val gradientBrush = brandGradientBrush()

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Normalize ratios
            val total = (principalRatio + interestRatio).coerceAtLeast(0.0001f)
            val principalSweep = (principalRatio / total) * 360f
            val interestSweep = 360f - principalSweep

            // Background circle / Interest segment (#3A3A3C)
            drawArc(
                color = DonutSecondary,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Principal segment (brand gradient)
            if (principalSweep > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = principalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        if (centerTitle.isNotBlank() || centerSubtitle.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (centerTitle.isNotBlank()) {
                    Text(
                        text = centerTitle,
                        color = colors.textDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
                if (centerSubtitle.isNotBlank()) {
                    Text(
                        text = centerSubtitle,
                        color = colors.textMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
