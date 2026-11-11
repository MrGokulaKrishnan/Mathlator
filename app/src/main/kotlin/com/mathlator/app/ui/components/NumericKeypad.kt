package com.mathlator.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Numeric keypad used by converters and Currency.
 *
 * 4-column grid layout with NO empty cells:
 * ```
 *  7   8   9   ⌫ (tall, spans rows 1–2)
 *  4   5   6   ⌫
 *  1   2   3   C (tall, spans rows 3–4)
 *  .   0   00  C
 * ```
 */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onSign: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    enabledDigits: Set<Char>? = null,
    showDecimal: Boolean = true,
    showSign: Boolean = false,
) {
    val colors = mathlatorColors
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun isEnabled(digit: Char) = enabledDigits == null || digit in enabledDigits

    @Composable
    fun NumKey(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        keyColor: androidx.compose.ui.graphics.Color = colors.textMain,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.93f else 1f,
            animationSpec = tween(80),
            label = "NumKeyScale",
        )
        Box(
            modifier = modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceHigh)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    enabled = enabled,
                    onClick = {
                        performHaptic()
                        onClick()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                color = if (enabled) keyColor else keyColor.copy(alpha = 0.3f),
            )
        }
    }

    @Composable
    fun DeleteKey(modifier: Modifier = Modifier) {
        var repeatJob: Job? = remember { null }
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.93f else 1f,
            animationSpec = tween(80),
            label = "DelKeyScale",
        )
        Box(
            modifier = modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceHigh)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            performHaptic()
                            onDelete()
                            repeatJob = scope.launch {
                                delay(400)
                                while (isActive) {
                                    performHaptic()
                                    onDelete()
                                    delay(60)
                                }
                            }
                            tryAwaitRelease()
                            repeatJob?.cancel()
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("⌫", fontSize = 24.sp, color = colors.operator)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val doubleRowH = 118.dp // 56dp * 2 + 6dp gap

        // Top Block: Rows 1 & 2 (7,8,9 / 4,5,6) + tall ⌫ key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(doubleRowH),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    NumKey("7", { onDigit("7") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('7'))
                    NumKey("8", { onDigit("8") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('8'))
                    NumKey("9", { onDigit("9") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('9'))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    NumKey("4", { onDigit("4") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('4'))
                    NumKey("5", { onDigit("5") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('5'))
                    NumKey("6", { onDigit("6") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('6'))
                }
            }
            DeleteKey(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Bottom Block: Rows 3 & 4 (1,2,3 / .,0,00) + tall C key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(doubleRowH),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    NumKey("1", { onDigit("1") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('1'))
                    NumKey("2", { onDigit("2") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('2'))
                    NumKey("3", { onDigit("3") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('3'))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (showDecimal) {
                        NumKey(".", onDecimal, Modifier.weight(1f).fillMaxHeight())
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceHigh),
                        )
                    }
                    NumKey("0", { onDigit("0") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('0'))
                    NumKey("00", { onDigit("0"); onDigit("0") }, Modifier.weight(1f).fillMaxHeight(), isEnabled('0'))
                }
            }
            NumKey(
                label = "C",
                onClick = onClear,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                keyColor = colors.operator,
            )
        }
    }
}
