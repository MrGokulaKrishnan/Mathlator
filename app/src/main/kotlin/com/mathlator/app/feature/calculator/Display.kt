package com.mathlator.app.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.theme.ExpressionTextStyle
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Display(
    expression: String,
    result: String,
    isError: Boolean,
    errorMessage: String,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    angleMode: String = "DEG",
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }

    // Auto-scroll expression line to end on input
    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Error shake animation (300ms)
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    0f at 0
                    -16f at 50
                    16f at 100
                    -12f at 150
                    12f at 200
                    -6f at 250
                    0f at 300
                }
            )
        }
    }

    // Dynamic text size for result (auto-shrinks from 48sp down to 28sp)
    val resultTextLength = result.length
    val dynamicResultFontSize = when {
        resultTextLength > 16 -> 28.sp
        resultTextLength > 12 -> 34.sp
        resultTextLength > 8  -> 40.sp
        else                  -> 48.sp
    }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .tacticalRecessedWell(
                cornerRadius = TacticalDimens.DisplayWellCornerRadius,
                isFocused = false,
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -20f) {
                            onBackspace()
                        }
                    }
                },
            horizontalAlignment = Alignment.End,
        ) {
            // Top Status Row: CALC // DEG micro-badge on left, Expression line on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CALC // $angleMode",
                    style = TacticalTypography.TelemetryMicroBadge,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .horizontalScroll(scrollState),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = expression.ifEmpty { " " },
                        style = ExpressionTextStyle.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            color = TacticalColors.TextSecondary,
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.semantics {
                            contentDescription = "Expression: $expression"
                        },
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Main Result Readout Line (Tabular monospace auto-sizing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            if (result.isNotBlank() && !isError) {
                                showMenu = true
                            }
                        }
                    ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                AnimatedContent(
                    targetState = when {
                        isError -> errorMessage.ifEmpty { "Invalid expression" }
                        result.isNotEmpty() -> "= $result"
                        else -> "0"
                    },
                    transitionSpec = {
                        (slideInVertically { height -> height / 2 } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
                    },
                    label = "ResultAnimation",
                ) { targetText ->
                    val textColor = when {
                        isError -> Color(0xFFFF4D4D)
                        result.isEmpty() -> TacticalColors.TextMuted
                        else -> TacticalColors.TextPrimary
                    }

                    Text(
                        text = targetText,
                        style = TacticalTypography.OledReadoutLarge.copy(
                            fontSize = dynamicResultFontSize,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Result: $targetText"
                        },
                    )
                }

                // Copy / Share Popup Menu on Long-Press
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy", color = TacticalColors.TextPrimary) },
                        onClick = {
                            showMenu = false
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Mathlator Result", result)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share", color = TacticalColors.TextPrimary) },
                        onClick = {
                            showMenu = false
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$expression = $result")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Calculation"))
                        }
                    )
                }
            }
        }
    }
}
