package com.mathlator.app.feature.calculator

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.ui.components.TacticalCalculatorKeypad
import com.mathlator.app.ui.theme.MathlatorTheme
import com.mathlator.app.ui.theme.mathlatorColors

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val haptics by settingsRepository.haptics.collectAsState(initial = true)
    val isOneHanded by settingsRepository.oneHandedEnabled.collectAsState(initial = false)
    val oneHandedSide by settingsRepository.oneHandedSide.collectAsState(initial = "right")

    CalculatorContent(
        state = state,
        haptics = haptics,
        isOneHanded = isOneHanded,
        oneHandedSide = oneHandedSide,
        onKey = { viewModel.onKey(it) },
        onDelete = { viewModel.backspace() },
        onClear = { viewModel.onKey("C") },
        onModeToggle = { viewModel.toggleScientificMode() },
        modifier = modifier,
    )
}

@Composable
fun CalculatorContent(
    state: CalculatorState,
    haptics: Boolean,
    isOneHanded: Boolean,
    oneHandedSide: String,
    onKey: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val colors = mathlatorColors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Constrain width to 480dp on large screens / tablets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (isLandscape) 900.dp else 480.dp),
        ) {
            if (isLandscape) {
                // Landscape: Display across top, SciPanel on left, Keypad on right
                Column(modifier = Modifier.fillMaxSize()) {
                    Display(
                        expression = state.displayExpression,
                        result = state.result,
                        isError = state.isError,
                        errorMessage = state.errorMessage,
                        onBackspace = onDelete,
                        angleMode = if (state.angleMode == AngleMode.DEG) "DEG" else "RAD",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.35f),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.65f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        SciPanel(
                            angleMode = state.angleMode,
                            isSecondFunction = state.isSecondFunction,
                            onKey = onKey,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .padding(end = 8.dp),
                        )
                        TacticalCalculatorKeypad(
                            onKey = onKey,
                            onDelete = onDelete,
                            onClear = onClear,
                            onModeToggle = onModeToggle,
                            isScientificActive = state.isScientific,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                // Portrait:
                // - Display is bottom-aligned in its weighted slot (weight = 0.9 normal, 0.6 sci)
                // - Divider sits directly above keypad
                // - Scientific panel slides in between display and keypad
                // - Keypad fills the remaining weight (1.6)
                Column(modifier = Modifier.fillMaxSize()) {

                    // Display slot — bottom-aligned so result sits just above the divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (state.isScientific) 0.6f else 0.9f),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Display(
                            expression = state.displayExpression,
                            result = state.result,
                            isError = state.isError,
                            errorMessage = state.errorMessage,
                            onBackspace = onDelete,
                            angleMode = if (state.angleMode == AngleMode.DEG) "DEG" else "RAD",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Scientific panel slides in/out above keypad
                    AnimatedVisibility(
                        visible = state.isScientific,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SciPanel(
                            angleMode = state.angleMode,
                            isSecondFunction = state.isSecondFunction,
                            onKey = onKey,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Keypad fills the bottom portion (one-handed shrinks width to 82%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.6f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = if (isOneHanded) {
                            if (oneHandedSide == "left") Alignment.BottomStart else Alignment.BottomEnd
                        } else {
                            Alignment.BottomCenter
                        },
                    ) {
                        TacticalCalculatorKeypad(
                            onKey = onKey,
                            onDelete = onDelete,
                            onClear = onClear,
                            onModeToggle = onModeToggle,
                            isScientificActive = state.isScientific,
                            modifier = if (isOneHanded) {
                                Modifier.fillMaxWidth(0.82f)
                            } else {
                                Modifier.fillMaxSize()
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Pixel Portrait Dark", device = "id:pixel_7", showSystemUi = true)
@Composable
private fun CalculatorPreviewPixel() {
    MathlatorTheme(isDark = true) {
        CalculatorContent(
            state = CalculatorState(displayExpression = "125 × 8", result = "1,000"),
            haptics = true,
            isOneHanded = false,
            oneHandedSide = "right",
            onKey = {},
            onDelete = {},
            onClear = {},
            onModeToggle = {},
        )
    }
}

@Preview(name = "Small Device 360x640", widthDp = 360, heightDp = 640)
@Composable
private fun CalculatorPreviewSmall() {
    MathlatorTheme(isDark = true) {
        CalculatorContent(
            state = CalculatorState(displayExpression = "", result = ""),
            haptics = true,
            isOneHanded = false,
            oneHandedSide = "right",
            onKey = {},
            onDelete = {},
            onClear = {},
            onModeToggle = {},
        )
    }
}

@Preview(name = "Landscape 800x360", widthDp = 800, heightDp = 360)
@Composable
private fun CalculatorPreviewLandscape() {
    MathlatorTheme(isDark = true) {
        CalculatorContent(
            state = CalculatorState(displayExpression = "sin(45)", result = "0.7071"),
            haptics = true,
            isOneHanded = false,
            oneHandedSide = "right",
            onKey = {},
            onDelete = {},
            onClear = {},
            onModeToggle = {},
        )
    }
}

@Preview(name = "Font Scale 1.3", fontScale = 1.3f)
@Composable
private fun CalculatorPreviewLargeFont() {
    MathlatorTheme(isDark = true) {
        CalculatorContent(
            state = CalculatorState(displayExpression = "45000 + 1200", result = "46,200"),
            haptics = true,
            isOneHanded = false,
            oneHandedSide = "right",
            onKey = {},
            onDelete = {},
            onClear = {},
            onModeToggle = {},
        )
    }
}

@Preview(name = "Light Theme")
@Composable
private fun CalculatorPreviewLight() {
    MathlatorTheme(isDark = false) {
        CalculatorContent(
            state = CalculatorState(displayExpression = "99 ÷ 3", result = "33"),
            haptics = true,
            isOneHanded = false,
            oneHandedSide = "right",
            onKey = {},
            onDelete = {},
            onClear = {},
            onModeToggle = {},
        )
    }
}
