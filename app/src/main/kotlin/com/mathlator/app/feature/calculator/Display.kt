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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.ExpressionTextStyle
import com.mathlator.app.ui.theme.ResultTextStyle
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.launch
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
) {
    val colors = mathlatorColors
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

    // Dynamic text size for result (auto-shrinks from 64sp down to 28sp)
    val resultTextLength = result.length
    val dynamicResultFontSize = when {
        resultTextLength > 16 -> 28.sp
        resultTextLength > 12 -> 36.sp
        resultTextLength > 8  -> 48.sp
        else                  -> 64.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -20f) {
                        onBackspace()
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End,
    ) {
        // Expression Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = expression.ifEmpty { " " },
                style = ExpressionTextStyle,
                color = colors.textMain.copy(alpha = 0.8f),
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription = "Expression: $expression"
                },
            )
        }

        // Result Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp)
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
                    isError -> colors.error
                    result.isEmpty() -> colors.textDim
                    else -> colors.textMain
                }

                Text(
                    text = targetText,
                    style = ResultTextStyle.copy(fontSize = dynamicResultFontSize),
                    color = textColor,
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
                    text = { Text("Copy", color = colors.textMain) },
                    onClick = {
                        showMenu = false
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Mathlator Result", result)
                        clipboard.setPrimaryClip(clip)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = colors.textMain) },
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

        // 1dp Divider under display
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.divider,
        )
    }
}
