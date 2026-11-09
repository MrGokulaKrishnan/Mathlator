package com.mathlator.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.DonutSecondary
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.mathlatorColors
import java.math.BigDecimal

/**
 * Line / Area chart comparing Invested amount vs Total Value over time.
 */
@Composable
fun LineChart(
    investedPoints: List<Pair<Int, BigDecimal>>,
    valuePoints: List<Pair<Int, BigDecimal>>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 180.dp,
) {
    val colors = mathlatorColors
    val gradientBrush = brandGradientBrush()

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = DonutSecondary,
            ) {}
            Spacer(Modifier.width(4.dp))
            Text("Invested", color = colors.textDim, fontSize = 12.sp)

            Spacer(Modifier.width(16.dp))
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = Primary,
            ) {}
            Spacer(Modifier.width(4.dp))
            Text("Total Value", color = colors.textMain, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                if (valuePoints.isEmpty() && investedPoints.isEmpty()) return@Canvas

                val count = maxOf(valuePoints.size, investedPoints.size)
                if (count <= 1) return@Canvas

                val maxVal = maxOf(
                    valuePoints.maxOfOrNull { it.second.toFloat() } ?: 1f,
                    investedPoints.maxOfOrNull { it.second.toFloat() } ?: 1f,
                    1f
                )

                val w = size.width
                val h = size.height - 24.dp.toPx()
                val stepX = w / (count - 1).coerceAtLeast(1)

                // Grid lines (horizontal)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (h / gridLines) * i
                    drawLine(
                        color = colors.divider,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // Draw Invested Line (dashed / dim line)
                if (investedPoints.isNotEmpty()) {
                    val pathInvested = Path()
                    investedPoints.forEachIndexed { idx, point ->
                        val x = idx * stepX
                        val y = h - (point.second.toFloat() / maxVal * h)
                        if (idx == 0) pathInvested.moveTo(x, y) else pathInvested.lineTo(x, y)
                    }
                    drawPath(
                        path = pathInvested,
                        color = DonutSecondary,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }

                // Draw Total Value Line and Fill Area
                if (valuePoints.isNotEmpty()) {
                    val pathValue = Path()
                    val areaPath = Path()

                    valuePoints.forEachIndexed { idx, point ->
                        val x = idx * stepX
                        val y = h - (point.second.toFloat() / maxVal * h)
                        if (idx == 0) {
                            pathValue.moveTo(x, y)
                            areaPath.moveTo(x, h)
                            areaPath.lineTo(x, y)
                        } else {
                            pathValue.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                    }

                    // Complete area path to bottom-right and bottom-left
                    val lastX = (valuePoints.size - 1) * stepX
                    areaPath.lineTo(lastX, h)
                    areaPath.close()

                    // Fill area under curve with soft gradient
                    drawPath(
                        path = areaPath,
                        brush = gradientBrush,
                        alpha = 0.15f,
                    )

                    // Line stroke
                    drawPath(
                        path = pathValue,
                        brush = gradientBrush,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
    }
}
