package com.mathlator.app.feature.calculator

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.mathlator.app.data.prefs.SettingsRepository
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
                // Landscape Layout: Display across top, SciPanel on left, Keypad on right
                Column(modifier = Modifier.fillMaxSize()) {
                    Display(
                        expression = state.displayExpression,
                        result = state.result,
                        isError = state.isError,
                        errorMessage = state.errorMessage,
                        onBackspace = { viewModel.backspace() },
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
                            onKey = { viewModel.onKey(it) },
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .padding(end = 8.dp),
                        )
                        Keypad(
                            onKey = { viewModel.onKey(it) },
                            onDelete = { viewModel.backspace() },
                            onClear = { viewModel.onKey("C") },
                            onModeToggle = { viewModel.toggleScientificMode() },
                            hapticsEnabled = haptics,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                // Portrait Layout: Display (~38% height), SciPanel (collapsible), Keypad
                Column(modifier = Modifier.fillMaxSize()) {
                    Display(
                        expression = state.displayExpression,
                        result = state.result,
                        isError = state.isError,
                        errorMessage = state.errorMessage,
                        onBackspace = { viewModel.backspace() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (state.isScientific) 0.30f else 0.38f),
                    )

                    // Scientific panel slides in/out above keypad
                    AnimatedVisibility(
                        visible = state.isScientific,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SciPanel(
                            angleMode = state.angleMode,
                            isSecondFunction = state.isSecondFunction,
                            onKey = { viewModel.onKey(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Keypad container (with one-handed mode width scaling to 82%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (state.isScientific) 0.70f else 0.62f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = if (isOneHanded) {
                            if (oneHandedSide == "left") Alignment.BottomStart else Alignment.BottomEnd
                        } else {
                            Alignment.BottomCenter
                        },
                    ) {
                        Keypad(
                            onKey = { viewModel.onKey(it) },
                            onDelete = { viewModel.backspace() },
                            onClear = { viewModel.onKey("C") },
                            onModeToggle = { viewModel.toggleScientificMode() },
                            hapticsEnabled = haptics,
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
