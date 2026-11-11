package com.mathlator.app.feature.calculator

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.KeyTextStyle
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun Keypad(
    onKey: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onModeToggle: () -> Unit,
    isScientificActive: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    fun performHaptic() {
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    @Composable
    fun StandardKey(
        label: String,
        contentDesc: String,
        keyColor: Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1.0f,
            animationSpec = tween(durationMillis = 80),
            label = "StandardKeyScale",
        )

        Box(
            modifier = modifier
                .fillMaxHeight()
                .scale(scale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = 36.dp),
                    onClick = {
                        performHaptic()
                        onClick()
                    },
                )
                .semantics { contentDescription = contentDesc },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = KeyTextStyle,
                color = keyColor,
            )
        }
    }

    // Backspace key with repeat delete every 60ms on hold
    @Composable
    fun BackspaceKey(modifier: Modifier = Modifier) {
        var repeatJob: Job? = remember { null }

        Box(
            modifier = modifier
                .fillMaxHeight()
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            performHaptic()
                            onDelete()
                            repeatJob = scope.launch {
                                delay(400) // Initial delay before repeating
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
                }
                .semantics { contentDescription = "delete" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "⌫",
                style = KeyTextStyle,
                color = colors.operator,
            )
        }
    }

    // Equals Key: 76dp circle with brand gradient and soft glow
    @Composable
    fun EqualsKey(modifier: Modifier = Modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1.0f,
            animationSpec = tween(durationMillis = 80),
            label = "EqualsKeyScale",
        )

        Box(
            modifier = modifier
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Primary.copy(alpha = 0.35f),
                        ambientColor = Primary.copy(alpha = 0.35f),
                    )
                    .clip(CircleShape)
                    .background(brandGradientBrush())
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 38.dp),
                        onClick = {
                            performHaptic()
                            onKey("=")
                        },
                    )
                    .semantics { contentDescription = "equals" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "=",
                    style = KeyTextStyle.copy(fontSize = 36.sp),
                    color = Color.White,
                )
            }
        }
    }

    // Mode Toggle Key — shows "f(x)" and highlights in Primary when Scientific mode is on
    @Composable
    fun ModeKey(modifier: Modifier = Modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1.0f,
            animationSpec = tween(durationMillis = 80),
            label = "ModeKeyScale",
        )

        Box(
            modifier = modifier
                .fillMaxHeight()
                .scale(scale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = 36.dp),
                    onClick = {
                        performHaptic()
                        onModeToggle()
                    },
                )
                .semantics { contentDescription = "toggle scientific mode" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "f(x)",
                style = KeyTextStyle.copy(fontSize = 18.sp),
                color = if (isScientificActive) Primary else colors.operator,
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Row 1: C ⌫ % ÷
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StandardKey("C", "clear all", colors.operator, onClear, Modifier.weight(1f))
            BackspaceKey(Modifier.weight(1f))
            StandardKey("%", "percent", colors.operator, { onKey("%") }, Modifier.weight(1f))
            StandardKey("÷", "divide", colors.operator, { onKey("÷") }, Modifier.weight(1f))
        }

        // Row 2: 7 8 9 ×
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StandardKey("7", "7", colors.textMain, { onKey("7") }, Modifier.weight(1f))
            StandardKey("8", "8", colors.textMain, { onKey("8") }, Modifier.weight(1f))
            StandardKey("9", "9", colors.textMain, { onKey("9") }, Modifier.weight(1f))
            StandardKey("×", "multiply", colors.operator, { onKey("×") }, Modifier.weight(1f))
        }

        // Row 3: 4 5 6 −
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StandardKey("4", "4", colors.textMain, { onKey("4") }, Modifier.weight(1f))
            StandardKey("5", "5", colors.textMain, { onKey("5") }, Modifier.weight(1f))
            StandardKey("6", "6", colors.textMain, { onKey("6") }, Modifier.weight(1f))
            StandardKey("−", "minus", colors.operator, { onKey("−") }, Modifier.weight(1f))
        }

        // Row 4: 1 2 3 +
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StandardKey("1", "1", colors.textMain, { onKey("1") }, Modifier.weight(1f))
            StandardKey("2", "2", colors.textMain, { onKey("2") }, Modifier.weight(1f))
            StandardKey("3", "3", colors.textMain, { onKey("3") }, Modifier.weight(1f))
            StandardKey("+", "plus", colors.operator, { onKey("+") }, Modifier.weight(1f))
        }

        // Row 5: [mode] 0 . [=]
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ModeKey(Modifier.weight(1f))
            StandardKey("0", "0", colors.textMain, { onKey("0") }, Modifier.weight(1f))
            StandardKey(".", "decimal point", colors.textMain, { onKey(".") }, Modifier.weight(1f))
            EqualsKey(Modifier.weight(1f))
        }
    }
}
