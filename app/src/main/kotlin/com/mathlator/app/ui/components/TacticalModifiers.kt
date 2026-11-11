package com.mathlator.app.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mathlator.app.ui.theme.TacticalColors

/**
 * Custom Neumorphic Tactical Dark hardware modifiers for Compose.
 */

/**
 * Extruded machined keycap with dual ambient/spot Neumorphic depth,
 * specular edge highlights, and tactile press animation.
 */
fun Modifier.tacticalExtrudedKeycap(
    cornerRadius: Dp = 20.dp,
    isPressed: Boolean,
    isAccent: Boolean = false,
): Modifier = this
    .graphicsLayer {
        val s = if (isPressed) 0.96f else 1.0f
        scaleX = s
        scaleY = s
    }
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithCache {
        val cornerRadiusPx = cornerRadius.toPx()
        val r = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        val darkPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(0xE6, 0x02, 0x03, 0x05)
            maskFilter = BlurMaskFilter(12.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
        }

        val lightPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(0x0D, 0xFF, 0xFF, 0xFF)
            maskFilter = BlurMaskFilter(8.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
        }

        val accentBloomPaint = if (isAccent) {
            AndroidPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb(0x66, 0xFF, 0x5E, 0x00)
                maskFilter = BlurMaskFilter(14.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
            }
        } else null

        val borderGradient = Brush.linearGradient(
            colors = if (isAccent) {
                listOf(Color(0x80FFFFFF), Color(0x66000000))
            } else {
                listOf(Color.White.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.60f))
            },
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )

        val innerTopShadow = Brush.verticalGradient(
            colors = listOf(Color(0xFF030406), Color.Transparent),
            startY = 0f,
            endY = 8.dp.toPx()
        )
        val innerLeftShadow = Brush.horizontalGradient(
            colors = listOf(Color(0xFF030406), Color.Transparent),
            startX = 0f,
            endX = 8.dp.toPx()
        )

        onDrawBehind {
            if (!isPressed) {
                // Accent bloom
                if (isAccent && accentBloomPaint != null) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f,
                            4.dp.toPx(),
                            size.width,
                            size.height + 4.dp.toPx(),
                            cornerRadiusPx,
                            cornerRadiusPx,
                            accentBloomPaint
                        )
                    }
                }

                // Bottom-right dark drop shadow (+5dp, +6dp)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        5.dp.toPx(),
                        6.dp.toPx(),
                        size.width + 5.dp.toPx(),
                        size.height + 6.dp.toPx(),
                        cornerRadiusPx,
                        cornerRadiusPx,
                        darkPaint
                    )
                }

                // Top-left ambient lift (-3dp, -3dp)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        -3.dp.toPx(),
                        -3.dp.toPx(),
                        size.width - 3.dp.toPx(),
                        size.height - 3.dp.toPx(),
                        cornerRadiusPx,
                        cornerRadiusPx,
                        lightPaint
                    )
                }
            }

            // 1dp inner specular border stroke along top/left fading to dark bottom/right
            drawRoundRect(
                brush = borderGradient,
                size = size,
                cornerRadius = r,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner recessed shadow on pressed state
            if (isPressed) {
                clipPath(Path().apply {
                    addRoundRect(RoundRect(Rect(Offset.Zero, size), r))
                }) {
                    drawRect(brush = innerTopShadow)
                    drawRect(brush = innerLeftShadow)
                }
            }
        }
    }

/**
 * Recessed OLED display readout well, input cavity, or telemetry well.
 * Renders a deep inset cavity (#0B0D12) with top-left dark shadow simulation and
 * an active glowing border when isFocused is true.
 */
fun Modifier.tacticalRecessedWell(
    cornerRadius: Dp = 22.dp,
    isFocused: Boolean = false,
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithCache {
        val cornerRadiusPx = cornerRadius.toPx()
        val r = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        val path = Path().apply {
            addRoundRect(RoundRect(Rect(Offset.Zero, size), r))
        }

        val topInnerShadow = Brush.verticalGradient(
            colors = listOf(Color(0xFF020305), Color.Transparent),
            startY = 0f,
            endY = 10.dp.toPx()
        )
        val leftInnerShadow = Brush.horizontalGradient(
            colors = listOf(Color(0xFF020305), Color.Transparent),
            startX = 0f,
            endX = 10.dp.toPx()
        )
        val bottomSpecular = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x0DFFFFFF)),
            startY = (size.height - 4.dp.toPx()).coerceAtLeast(0f),
            endY = size.height
        )

        val glowPaint = if (isFocused) {
            AndroidPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb(0x38, 0xFF, 0x5E, 0x00)
                maskFilter = BlurMaskFilter(8.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
            }
        } else null

        onDrawBehind {
            // Fill cavity (#0B0D12)
            drawRoundRect(
                color = if (isFocused) TacticalColors.SurfaceRecessedWellActive else TacticalColors.SurfaceRecessedWell,
                size = size,
                cornerRadius = r
            )

            // Outer orange glow when focused
            if (isFocused && glowPaint != null) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        0f,
                        0f,
                        size.width,
                        size.height,
                        cornerRadiusPx,
                        cornerRadiusPx,
                        glowPaint
                    )
                }
            }

            // Inset cavity shadow simulation
            clipPath(path) {
                drawRect(brush = topInnerShadow)
                drawRect(brush = leftInnerShadow)
                drawRect(brush = bottomSpecular)
            }

            // 1.5dp border: TacticalOrange when focused, or subtle white 5% when inactive
            if (isFocused) {
                drawRoundRect(
                    color = TacticalColors.TacticalOrange,
                    size = size,
                    cornerRadius = r,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            } else {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.05f),
                    size = size,
                    cornerRadius = r,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }

/**
 * Extruded Bento tile / Instrument card surface modifier.
 */
fun Modifier.tacticalCard(
    cornerRadius: Dp = 20.dp,
    isPressed: Boolean = false,
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithCache {
        val cornerRadiusPx = cornerRadius.toPx()
        val r = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        val darkPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(0xCC, 0x02, 0x03, 0x05)
            maskFilter = BlurMaskFilter(10.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
        }
        val lightPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(0x0A, 0xFF, 0xFF, 0xFF)
            maskFilter = BlurMaskFilter(6.dp.toPx().coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
        }

        val borderGradient = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )

        onDrawBehind {
            if (!isPressed) {
                // Drop shadow
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        4.dp.toPx(),
                        5.dp.toPx(),
                        size.width + 4.dp.toPx(),
                        size.height + 5.dp.toPx(),
                        cornerRadiusPx,
                        cornerRadiusPx,
                        darkPaint
                    )
                }
                // Ambient lift
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        -2.dp.toPx(),
                        -2.dp.toPx(),
                        size.width - 2.dp.toPx(),
                        size.height - 2.dp.toPx(),
                        cornerRadiusPx,
                        cornerRadiusPx,
                        lightPaint
                    )
                }
            }

            // Machined gradient surface
            drawRoundRect(
                brush = TacticalColors.SurfaceCardGradient,
                size = size,
                cornerRadius = r
            )

            // Specular perimeter border
            drawRoundRect(
                brush = borderGradient,
                size = size,
                cornerRadius = r,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }

/**
 * Etched socket well for housing icons inside Bento tiles.
 */
fun Modifier.tacticalSocket(
    cornerRadius: Dp = 14.dp,
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithCache {
        val cornerRadiusPx = cornerRadius.toPx()
        val r = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        onDrawBehind {
            drawRoundRect(
                color = TacticalColors.SurfaceSocket,
                size = size,
                cornerRadius = r
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.05f),
                size = size,
                cornerRadius = r,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
