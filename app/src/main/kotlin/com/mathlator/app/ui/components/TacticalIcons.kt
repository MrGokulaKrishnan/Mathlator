package com.mathlator.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object TacticalIcons {
    private fun vector(
        name: String,
        viewportSize: Float = 24f,
        strokeWidth: Float = 2.4f,
        paths: List<String>,
        fillPaths: List<String> = emptyList(),
    ): ImageVector {
        val builder = ImageVector.Builder(
            name = name,
            defaultWidth = viewportSize.dp,
            defaultHeight = viewportSize.dp,
            viewportWidth = viewportSize,
            viewportHeight = viewportSize,
        )

        fillPaths.forEach { fillData ->
            builder.addPath(
                pathData = PathParser().parsePathString(fillData).toNodes(),
                fill = SolidColor(Color.White.copy(alpha = 0.16f)),
                stroke = null,
            )
        }

        paths.forEach { strokeData ->
            builder.addPath(
                pathData = PathParser().parsePathString(strokeData).toNodes(),
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }

        return builder.build()
    }

    // ── Keypad Operators & Utility zone ───────────────────────────────────────
    val Clear: ImageVector = vector(
        name = "Clear",
        viewportSize = 28f,
        strokeWidth = 2.6f,
        paths = listOf(
            "M 20 9.5 A 7.5 7.5 0 1 0 20 18.5"
        )
    )

    val Backspace: ImageVector = vector(
        name = "Backspace",
        viewportSize = 28f,
        strokeWidth = 2.4f,
        paths = listOf(
            "M 10.4 6.8 H 20.6 A 2.8 2.8 0 0 1 23.4 9.6 v 8.8 a 2.8 2.8 0 0 1 -2.8 2.8 H 10.4 a 2.8 2.8 0 0 1 -2.15 -1 L 4.1 15.3 a 2 2 0 0 1 0 -2.6 l 4.15 -4.9 a 2.8 2.8 0 0 1 2.15 -1 Z",
            "M 13.5 11.5 L 18.5 16.5 M 18.5 11.5 L 13.5 16.5"
        ),
        fillPaths = listOf(
            "M 10.4 6.8 H 20.6 A 2.8 2.8 0 0 1 23.4 9.6 v 8.8 a 2.8 2.8 0 0 1 -2.8 2.8 H 10.4 a 2.8 2.8 0 0 1 -2.15 -1 L 4.1 15.3 a 2 2 0 0 1 0 -2.6 l 4.15 -4.9 a 2.8 2.8 0 0 1 2.15 -1 Z"
        )
    )

    val Percent: ImageVector = vector(
        name = "Percent",
        viewportSize = 28f,
        strokeWidth = 2.4f,
        paths = listOf(
            "M 8.5 19.5 L 19.5 8.5",
            "M 7.2 9.5 A 2.3 2.3 0 1 0 11.8 9.5 A 2.3 2.3 0 1 0 7.2 9.5",
            "M 16.2 18.5 A 2.3 2.3 0 1 0 20.8 18.5 A 2.3 2.3 0 1 0 16.2 18.5"
        )
    )

    val Divide: ImageVector = vector(
        name = "Divide",
        viewportSize = 28f,
        strokeWidth = 2.6f,
        paths = listOf(
            "M 7.5 14 H 20.5",
            "M 12.1 8.2 A 1.9 1.9 0 1 0 15.9 8.2 A 1.9 1.9 0 1 0 12.1 8.2",
            "M 12.1 19.8 A 1.9 1.9 0 1 0 15.9 19.8 A 1.9 1.9 0 1 0 12.1 19.8"
        )
    )

    val Multiply: ImageVector = vector(
        name = "Multiply",
        viewportSize = 28f,
        strokeWidth = 2.6f,
        paths = listOf(
            "M 8.8 8.8 L 19.2 19.2 M 19.2 8.8 L 8.8 19.2"
        )
    )

    val Subtract: ImageVector = vector(
        name = "Subtract",
        viewportSize = 28f,
        strokeWidth = 2.6f,
        paths = listOf(
            "M 7.5 14 H 20.5"
        )
    )

    val Add: ImageVector = vector(
        name = "Add",
        viewportSize = 28f,
        strokeWidth = 2.6f,
        paths = listOf(
            "M 14 7.5 V 20.5 M 7.5 14 H 20.5"
        )
    )

    val Equals: ImageVector = vector(
        name = "Equals",
        viewportSize = 28f,
        strokeWidth = 2.8f,
        paths = listOf(
            "M 7.5 11.2 H 20.5 M 7.5 16.8 H 20.5"
        )
    )

    val Swap: ImageVector = vector(
        name = "Swap",
        viewportSize = 24f,
        strokeWidth = 2.4f,
        paths = listOf(
            "M 7 16 V 4 M 3 8 L 7 4 L 11 8 M 17 8 V 20 M 13 16 L 17 20 L 21 16"
        )
    )

    val Copy: ImageVector = vector(
        name = "Copy",
        viewportSize = 24f,
        strokeWidth = 2.2f,
        paths = listOf(
            "M 9 9 H 20 A 2 2 0 0 1 22 11 V 20 A 2 2 0 0 1 20 22 H 9 A 2 2 0 0 1 7 20 V 11 A 2 2 0 0 1 9 9 Z",
            "M 5 15 H 4 A 2 2 0 0 1 2 13 V 4 A 2 2 0 0 1 4 2 H 13 A 2 2 0 0 1 15 4 V 5"
        )
    )

    // ── Segmented Dock Navigation Icons ───────────────────────────────────────
    val Calculator: ImageVector = vector(
        name = "Calculator",
        viewportSize = 24f,
        strokeWidth = 2.1f,
        paths = listOf(
            "M 7 4 H 17 A 3 3 0 0 1 20 7 V 17 A 3 3 0 0 1 17 20 H 7 A 3 3 0 0 1 4 7 V 7 A 3 3 0 0 1 7 4 Z",
            "M 7.5 9 H 10.5 M 13.5 7.8 L 15.9 10.2 M 15.9 7.8 L 13.5 10.2 M 9 13.8 V 16.8 M 7.5 15.3 H 10.5 M 13.5 14.2 H 16.5 M 13.5 16.5 H 16.5"
        )
    )

    val BentoGrid: ImageVector = vector(
        name = "BentoGrid",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 5.5 4 H 9 A 1.5 1.5 0 0 1 10.5 5.5 V 9 A 1.5 1.5 0 0 1 9 10.5 H 5.5 A 1.5 1.5 0 0 1 4 9 V 5.5 A 1.5 1.5 0 0 1 5.5 4 Z",
            "M 15 4 H 18.5 A 1.5 1.5 0 0 1 20 5.5 V 9 A 1.5 1.5 0 0 1 18.5 10.5 H 15 A 1.5 1.5 0 0 1 13.5 9 V 5.5 A 1.5 1.5 0 0 1 15 4 Z",
            "M 5.5 13.5 H 9 A 1.5 1.5 0 0 1 10.5 15 V 18.5 A 1.5 1.5 0 0 1 9 20 H 5.5 A 1.5 1.5 0 0 1 4 18.5 V 15 A 1.5 1.5 0 0 1 5.5 13.5 Z",
            "M 15 13.5 H 18.5 A 1.5 1.5 0 0 1 20 15 V 18.5 A 1.5 1.5 0 0 1 18.5 20 H 15 A 1.5 1.5 0 0 1 13.5 18.5 V 15 A 1.5 1.5 0 0 1 15 13.5 Z"
        )
    )

    val FinanceHub: ImageVector = vector(
        name = "FinanceHub",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 21 12 A 9 9 0 0 0 5.7 5.6 L 3 8",
            "M 3 3 V 8 H 8",
            "M 3 12 A 9 9 0 0 0 18.3 18.4 L 21 16",
            "M 16 16 H 21 V 21",
            "M 12 7.5 V 16.5 M 10 9.5 C 10 8.4 10.9 7.9 12 7.9 C 13.1 7.9 14 8.4 14 9.5 C 14 11.5 10 11.1 10 13.1 C 10 14.2 10.9 14.7 12 14.7 C 13.1 14.7 14 14.2 14 13.1"
        )
    )

    // ── Bento Converters Tool Glyphs ──────────────────────────────────────────
    val Age: ImageVector = vector(
        name = "Age",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 8 8 A 4 4 0 1 0 16 8 A 4 4 0 1 0 8 8 Z",
            "M 5 20 C 6.5 16 17.5 16 19 20"
        ),
        fillPaths = listOf(
            "M 8 8 A 4 4 0 1 0 16 8 A 4 4 0 1 0 8 8 Z"
        )
    )

    val Area: ImageVector = vector(
        name = "Area",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 4 9 V 5 A 1 1 0 0 1 5 4 H 9 M 20 9 V 5 A 1 1 0 0 0 19 4 H 15 M 4 15 V 19 A 1 1 0 0 0 5 20 H 9 M 20 15 V 19 A 1 1 0 0 1 19 20 H 15",
            "M 9 8 H 15 A 1 1 0 0 1 16 9 V 15 A 1 1 0 0 1 15 16 H 9 A 1 1 0 0 1 8 15 V 9 A 1 1 0 0 1 9 8 Z"
        ),
        fillPaths = listOf(
            "M 9 8 H 15 A 1 1 0 0 1 16 9 V 15 A 1 1 0 0 1 15 16 H 9 A 1 1 0 0 1 8 15 V 9 A 1 1 0 0 1 9 8 Z"
        )
    )

    val Bmi: ImageVector = vector(
        name = "Bmi",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 6.5 6.5 L 17.5 17.5",
            "M 3 10 L 10 3 M 14 21 L 21 14 M 2 6 L 6 2 M 18 22 L 22 18"
        )
    )

    val Data: ImageVector = vector(
        name = "Data",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 9.5 6 A 2.5 2.5 0 1 0 14.5 6 A 2.5 2.5 0 1 0 9.5 6 Z",
            "M 5 15 H 8 A 1 1 0 0 1 9 16 V 19 A 1 1 0 0 1 8 20 H 5 A 1 1 0 0 1 4 19 V 16 A 1 1 0 0 1 5 15 Z",
            "M 16 15 H 19 A 1 1 0 0 1 20 16 V 19 A 1 1 0 0 1 19 20 H 16 A 1 1 0 0 1 15 19 V 16 A 1 1 0 0 1 16 15 Z",
            "M 12 8.5 V 12 M 6.5 15 V 12 H 17.5 V 15"
        )
    )

    val Date: ImageVector = vector(
        name = "Date",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 6 4 H 18 A 3 3 0 0 1 21 7 V 18 A 3 3 0 0 1 18 21 H 6 A 3 3 0 0 1 3 7 V 7 A 3 3 0 0 1 6 4 Z",
            "M 16 2 V 6 M 8 2 V 6 M 3 9 H 21"
        ),
        fillPaths = listOf(
            "M 6 4 H 18 A 3 3 0 0 1 21 7 V 18 A 3 3 0 0 1 18 21 H 6 A 3 3 0 0 1 3 7 V 7 A 3 3 0 0 1 6 4 Z"
        )
    )

    val Discount: ImageVector = vector(
        name = "Discount",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 3 7.5 V 3 H 7.5 L 19 14.5 L 14.5 19 L 3 7.5 Z",
            "M 7.5 7.5 H 7.51 M 21 16.5 L 16.5 21"
        ),
        fillPaths = listOf(
            "M 3 7.5 V 3 H 7.5 L 19 14.5 L 14.5 19 L 3 7.5 Z"
        )
    )

    val Length: ImageVector = vector(
        name = "Length",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 4 7 H 20 A 2 2 0 0 1 22 9 V 15 A 2 2 0 0 1 20 17 H 4 A 2 2 0 0 1 2 15 V 9 A 2 2 0 0 1 4 7 Z",
            "M 6 7 V 11 M 10 7 V 10 M 14 7 V 11 M 18 7 V 10"
        ),
        fillPaths = listOf(
            "M 4 7 H 20 A 2 2 0 0 1 22 9 V 15 A 2 2 0 0 1 20 17 H 4 A 2 2 0 0 1 2 15 V 9 A 2 2 0 0 1 4 7 Z"
        )
    )

    val Mass: ImageVector = vector(
        name = "Mass",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 6 4 H 18 L 15 9 H 9 L 6 4 Z",
            "M 5 20 H 19 L 17.5 12 H 6.5 L 5 20 Z",
            "M 12 16 L 14 14"
        ),
        fillPaths = listOf(
            "M 5 20 H 19 L 17.5 12 H 6.5 L 5 20 Z"
        )
    )

    val Numeral: ImageVector = vector(
        name = "Numeral",
        viewportSize = 24f,
        strokeWidth = 2.2f,
        paths = listOf(
            "M 4 9 H 20 M 4 15 H 20 M 10 3 L 8 21 M 16 3 L 14 21"
        )
    )

    val Speed: ImageVector = vector(
        name = "Speed",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 4.9 19.1 A 10 10 0 1 1 19.1 19.1",
            "M 12 14 L 16 10",
            "M 10.5 14 A 1.5 1.5 0 1 0 13.5 14 A 1.5 1.5 0 1 0 10.5 14 Z"
        )
    )

    val Temperature: ImageVector = vector(
        name = "Temperature",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 14 14.76 V 5 A 2 2 0 1 0 10 5 V 14.76 A 4 4 0 1 0 14 14.76 Z",
            "M 10.5 17.5 A 1.5 1.5 0 1 0 13.5 17.5 A 1.5 1.5 0 1 0 10.5 17.5 Z"
        ),
        fillPaths = listOf(
            "M 14 14.76 V 5 A 2 2 0 1 0 10 5 V 14.76 A 4 4 0 1 0 14 14.76 Z"
        )
    )

    val Time: ImageVector = vector(
        name = "Time",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 3 12 A 9 9 0 1 0 21 12 A 9 9 0 1 0 3 12 Z",
            "M 12 7 V 12 L 15.5 14"
        ),
        fillPaths = listOf(
            "M 3 12 A 9 9 0 1 0 21 12 A 9 9 0 1 0 3 12 Z"
        )
    )

    val Volume: ImageVector = vector(
        name = "Volume",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 12 7 L 18 10.5 V 17.5 L 12 21 L 6 17.5 V 10.5 L 12 7 Z",
            "M 6 10.5 L 12 14 L 18 10.5 M 12 14 V 21",
            "M 4 7 V 4 H 7 M 20 7 V 4 H 17"
        ),
        fillPaths = listOf(
            "M 12 7 L 18 10.5 V 17.5 L 12 21 L 6 17.5 V 10.5 L 12 7 Z"
        )
    )

    // ── Finance Instrument Glyphs ─────────────────────────────────────────────
    val Gst: ImageVector = vector(
        name = "Gst",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 5 3 H 19 V 21 L 16.5 19.2 L 14 21 L 12 19.2 L 10 21 L 7.5 19.2 L 5 21 V 3 Z",
            "M 9 8 H 15 M 9 12 H 15 M 9 16 H 12"
        ),
        fillPaths = listOf(
            "M 5 3 H 19 V 21 L 16.5 19.2 L 14 21 L 12 19.2 L 10 21 L 7.5 19.2 L 5 21 V 3 Z"
        )
    )

    val Currency: ImageVector = vector(
        name = "Currency",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 3.5 12 A 8.5 8.5 0 1 0 20.5 12 A 8.5 8.5 0 1 0 3.5 12 Z",
            "M 12 7.5 V 16.5 M 10 9.5 C 10 8.5 10.8 8 12 8 C 13.2 8 14 8.5 14 9.5 C 14 11.5 10 11 10 13 C 10 14 10.8 14.5 12 14.5 C 13.2 14.5 14 14 14 13"
        ),
        fillPaths = listOf(
            "M 3.5 12 A 8.5 8.5 0 1 0 20.5 12 A 8.5 8.5 0 1 0 3.5 12 Z"
        )
    )

    val Investment: ImageVector = vector(
        name = "Investment",
        viewportSize = 24f,
        strokeWidth = 2.2f,
        paths = listOf(
            "M 3 17 L 9 11 L 13 15 L 21 7",
            "M 16 7 H 21 V 12"
        )
    )

    val Loan: ImageVector = vector(
        name = "Loan",
        viewportSize = 24f,
        strokeWidth = 2.0f,
        paths = listOf(
            "M 3 10 L 12 4 L 21 10 V 12 H 3 V 10 Z",
            "M 6 12 V 18 M 10 12 V 18 M 14 12 V 18 M 18 12 V 18 M 3 18 H 21"
        ),
        fillPaths = listOf(
            "M 3 10 L 12 4 L 21 10 V 12 H 3 V 10 Z"
        )
    )
}
