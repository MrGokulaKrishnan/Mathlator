package com.mathlator.app.ui.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Base tactile keycap button with Neumorphic dual-shadow extrusion and haptic response.
 */
@Composable
fun TacticalKeycapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = TacticalColors.KeycapNumBase,
    cornerRadius: Dp = TacticalDimens.KeycapCornerRadius,
    isAccent: Boolean = false,
    contentDescription: String? = null,
    onLongPress: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Box(
        modifier = modifier
            .tacticalExtrudedKeycap(
                cornerRadius = cornerRadius,
                isPressed = isPressed,
                isAccent = isAccent,
            )
            .background(brush)
            .then(
                if (onLongPress != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                performHaptic()
                                var repeatJob: Job? = null
                                try {
                                    onClick()
                                    repeatJob = scope.launch {
                                        delay(400)
                                        while (isActive) {
                                            performHaptic()
                                            onLongPress()
                                            delay(70)
                                        }
                                    }
                                    tryAwaitRelease()
                                } finally {
                                    repeatJob?.cancel()
                                }
                            }
                        )
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 32.dp),
                        onClick = {
                            performHaptic()
                            onClick()
                        }
                    )
                }
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * 5×4 Main Tactical Keypad for CalculatorScreen:
 * Row 1: [ C ], [ ⌫ ], [ % ], [ ÷ ]
 * Row 2: [ 7 ], [ 8 ], [ 9 ], [ × ]
 * Row 3: [ 4 ], [ 5 ], [ 6 ], [ − ]
 * Row 4: [ 1 ], [ 2 ], [ 3 ], [ + ]
 * Row 5: [ f(x) ], [ 0 ], [ . ], [ = ]
 */
@Composable
fun TacticalCalculatorKeypad(
    onKey: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onModeToggle: () -> Unit,
    isScientificActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
    ) {
        // Row 1: [ C ], [ ⌫ ], [ % ], [ ÷ ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            TacticalKeycapButton(
                onClick = onClear,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "All Clear",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = "AC",
                    style = TacticalTypography.KeycapUtilLabel.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = TacticalColors.TacticalOrange,
                )
            }
            TacticalKeycapButton(
                onClick = onDelete,
                onLongPress = onDelete,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Backspace",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Backspace,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(26.dp),
                )
            }
            TacticalKeycapButton(
                onClick = { onKey("%") },
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Percent",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Percent,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
            TacticalKeycapButton(
                onClick = { onKey("÷") },
                brush = TacticalColors.KeycapOpBase,
                contentDescription = "Divide",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Divide,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(26.dp),
                )
            }
        }

        // Row 2: [ 7 ], [ 8 ], [ 9 ], [ × ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "7", onClick = { onKey("7") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "8", onClick = { onKey("8") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "9", onClick = { onKey("9") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = { onKey("×") },
                brush = TacticalColors.KeycapOpBase,
                contentDescription = "Multiply",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Multiply,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Row 3: [ 4 ], [ 5 ], [ 6 ], [ − ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "4", onClick = { onKey("4") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "5", onClick = { onKey("5") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "6", onClick = { onKey("6") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = { onKey("−") },
                brush = TacticalColors.KeycapOpBase,
                contentDescription = "Subtract",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Subtract,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Row 4: [ 1 ], [ 2 ], [ 3 ], [ + ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "1", onClick = { onKey("1") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "2", onClick = { onKey("2") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "3", onClick = { onKey("3") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = { onKey("+") },
                brush = TacticalColors.KeycapOpBase,
                contentDescription = "Add",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Add,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Row 5: [ f(x) ], [ 0 ], [ . ], [ = ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            TacticalKeycapButton(
                onClick = onModeToggle,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Scientific panel toggle",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Text(
                    text = "f(x)",
                    style = TacticalTypography.KeycapUtilLabel.copy(
                        color = if (isScientificActive) TacticalColors.TacticalOrange else TacticalColors.TextSecondary,
                        fontSize = 17.sp,
                    ),
                )
            }
            NumericKey(label = "0", onClick = { onKey("0") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = ".", onClick = { onKey(".") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = { onKey("=") },
                brush = TacticalColors.KeycapEqualsBase,
                isAccent = true,
                contentDescription = "Equals",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Equals,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

/**
 * Strict 4×4 Uniform Aspect Grid Keypad for Unit Converters and Currency:
 * Row 1: [ 7 ], [ 8 ], [ 9 ], [ ⌫ Backspace ]
 * Row 2: [ 4 ], [ 5 ], [ 6 ], [ C Clear ]
 * Row 3: [ 1 ], [ 2 ], [ 3 ], [ ⇅ Swap Units ]
 * Row 4: [ . / ± ], [ 0 ], [ 00 ], [ 📋 Copy Result ]
 */
@Composable
fun TacticalConverterKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSwap: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    onSign: (() -> Unit)? = null,
    showSign: Boolean = false,
    showDecimal: Boolean = true,
    enabledDigits: Set<Char>? = null,
) {
    fun isEnabled(digit: Char) = enabledDigits == null || digit in enabledDigits

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
    ) {
        // Row 1: 7, 8, 9, ⌫
        Row(
            modifier = Modifier.fillMaxWidth().height(TacticalDimens.KeypadKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "7", enabled = isEnabled('7'), onClick = { onDigit("7") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "8", enabled = isEnabled('8'), onClick = { onDigit("8") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "9", enabled = isEnabled('9'), onClick = { onDigit("9") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = onDelete,
                onLongPress = onDelete,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Backspace",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Backspace,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Row 2: 4, 5, 6, C
        Row(
            modifier = Modifier.fillMaxWidth().height(TacticalDimens.KeypadKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "4", enabled = isEnabled('4'), onClick = { onDigit("4") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "5", enabled = isEnabled('5'), onClick = { onDigit("5") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "6", enabled = isEnabled('6'), onClick = { onDigit("6") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = onClear,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "All Clear",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Text(
                    text = "AC",
                    style = TacticalTypography.KeycapUtilLabel.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = TacticalColors.TacticalOrange,
                )
            }
        }

        // Row 3: 1, 2, 3, ⇅
        Row(
            modifier = Modifier.fillMaxWidth().height(TacticalDimens.KeypadKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            NumericKey(label = "1", enabled = isEnabled('1'), onClick = { onDigit("1") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "2", enabled = isEnabled('2'), onClick = { onDigit("2") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "3", enabled = isEnabled('3'), onClick = { onDigit("3") }, modifier = Modifier.weight(1f).fillMaxHeight())
            TacticalKeycapButton(
                onClick = onSwap,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Swap units",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Swap,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Row 4: . / ±, 0, 00, 📋
        Row(
            modifier = Modifier.fillMaxWidth().height(TacticalDimens.KeypadKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(TacticalDimens.KeypadSpacing),
        ) {
            if (showSign && onSign != null) {
                TacticalKeycapButton(
                    onClick = onSign,
                    brush = TacticalColors.KeycapNumBase,
                    contentDescription = "Sign toggle",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    Text(text = "±", style = TacticalTypography.KeycapNumLabel)
                }
            } else if (showDecimal) {
                NumericKey(label = ".", onClick = onDecimal, modifier = Modifier.weight(1f).fillMaxHeight())
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxHeight())
            }

            NumericKey(label = "0", enabled = isEnabled('0'), onClick = { onDigit("0") }, modifier = Modifier.weight(1f).fillMaxHeight())
            NumericKey(label = "00", enabled = isEnabled('0'), onClick = { onDigit("00") }, modifier = Modifier.weight(1f).fillMaxHeight())

            TacticalKeycapButton(
                onClick = onCopy,
                brush = TacticalColors.KeycapUtilBase,
                contentDescription = "Copy result",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Icon(
                    imageVector = TacticalIcons.Copy,
                    contentDescription = null,
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun NumericKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TacticalKeycapButton(
        onClick = onClick,
        brush = TacticalColors.KeycapNumBase,
        contentDescription = label,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = TacticalTypography.KeycapNumLabel.copy(
                color = if (enabled) TacticalColors.TextPrimary else TacticalColors.TextMuted,
            ),
        )
    }
}
