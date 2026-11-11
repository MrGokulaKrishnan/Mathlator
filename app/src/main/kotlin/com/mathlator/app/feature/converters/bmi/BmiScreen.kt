package com.mathlator.app.feature.converters.bmi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.tacticalCard
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import com.mathlator.app.ui.theme.TitleTextStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(
    viewModel: BmiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalColors.ObsidianChassis,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("BMI Biometric Analyzer", style = TitleTextStyle, color = TacticalColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TacticalColors.TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalColors.ObsidianChassis),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ── Top Segmented Toggle: Metric vs Imperial ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TacticalColors.SurfaceDock)
                        .padding(3.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SegmentedModeItem(
                            label = "Metric (cm / kg)",
                            isSelected = state.unitSystem == UnitSystem.METRIC,
                            onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                        )
                        SegmentedModeItem(
                            label = "Imperial (ft+in / lb)",
                            isSelected = state.unitSystem == UnitSystem.IMPERIAL,
                            onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                        )
                    }
                }
            }

            // ── Interactive Stepper Cards ─────────────────────────────────────
            if (state.unitSystem == UnitSystem.METRIC) {
                StepperInputCard(
                    label = "HEIGHT (CM)",
                    value = state.heightCm,
                    onValueChange = { viewModel.setHeightCm(it) },
                    onDecrement = {
                        val curr = state.heightCm.toIntOrNull() ?: 175
                        viewModel.setHeightCm((curr - 1).coerceAtLeast(50).toString())
                    },
                    onIncrement = {
                        val curr = state.heightCm.toIntOrNull() ?: 175
                        viewModel.setHeightCm((curr + 1).coerceAtMost(250).toString())
                    },
                )

                Spacer(Modifier.height(10.dp))

                StepperInputCard(
                    label = "WEIGHT (KG)",
                    value = state.weightKg,
                    onValueChange = { viewModel.setWeightKg(it) },
                    onDecrement = {
                        val curr = state.weightKg.toIntOrNull() ?: 70
                        viewModel.setWeightKg((curr - 1).coerceAtLeast(20).toString())
                    },
                    onIncrement = {
                        val curr = state.weightKg.toIntOrNull() ?: 70
                        viewModel.setWeightKg((curr + 1).coerceAtMost(300).toString())
                    },
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StepperInputCard(
                        label = "HEIGHT (FT)",
                        value = state.heightFt,
                        onValueChange = { viewModel.setHeightFt(it) },
                        onDecrement = {
                            val curr = state.heightFt.toIntOrNull() ?: 5
                            viewModel.setHeightFt((curr - 1).coerceAtLeast(2).toString())
                        },
                        onIncrement = {
                            val curr = state.heightFt.toIntOrNull() ?: 5
                            viewModel.setHeightFt((curr + 1).coerceAtMost(8).toString())
                        },
                        modifier = Modifier.weight(1f),
                    )
                    StepperInputCard(
                        label = "HEIGHT (IN)",
                        value = state.heightIn,
                        onValueChange = { viewModel.setHeightIn(it) },
                        onDecrement = {
                            val curr = state.heightIn.toIntOrNull() ?: 9
                            viewModel.setHeightIn((curr - 1).coerceAtLeast(0).toString())
                        },
                        onIncrement = {
                            val curr = state.heightIn.toIntOrNull() ?: 9
                            viewModel.setHeightIn((curr + 1).coerceAtMost(11).toString())
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(10.dp))

                StepperInputCard(
                    label = "WEIGHT (LB)",
                    value = state.weightLb,
                    onValueChange = { viewModel.setWeightLb(it) },
                    onDecrement = {
                        val curr = state.weightLb.toIntOrNull() ?: 154
                        viewModel.setWeightLb((curr - 1).coerceAtLeast(40).toString())
                    },
                    onIncrement = {
                        val curr = state.weightLb.toIntOrNull() ?: 154
                        viewModel.setWeightLb((curr + 1).coerceAtMost(600).toString())
                    },
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Asian Cut-Offs Toggle Card ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tacticalCard(cornerRadius = 16.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Asian cut-offs", color = TacticalColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Overweight ≥ 23, Obese ≥ 25", color = TacticalColors.TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = state.isAsianCutoffs,
                        onCheckedChange = { viewModel.setAsianCutoffs(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TacticalColors.TacticalOrange,
                            uncheckedTrackColor = TacticalColors.SurfaceSocket,
                            uncheckedThumbColor = TacticalColors.TextMuted,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── 4-Zone Biometric Spectrum Gauge ───────────────────────────────
            BiometricSpectrumGauge(
                bmiScore = state.bmiValue.toDoubleOrNull() ?: 22.0,
                currentCategory = state.categoryLabel,
            )

            Spacer(Modifier.height(18.dp))

            // ── Hero Result Card ──────────────────────────────────────────────
            val statusColor = when (state.categoryLabel) {
                "Underweight" -> Color(0xFF38BDF8)
                "Normal" -> TacticalColors.EmeraldTelemetry
                "Overweight" -> TacticalColors.TacticalAmber
                else -> Color(0xFFEF4444)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tacticalRecessedWell(
                        cornerRadius = TacticalDimens.WellCornerRadius,
                        isFocused = true,
                    )
                    .padding(18.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "BIOMETRIC SCORE",
                                style = TacticalTypography.TelemetryMicroBadge,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${state.bmiValue} — ${state.categoryLabel}",
                                style = TacticalTypography.OledReadoutMedium.copy(
                                    fontSize = 24.sp,
                                    color = statusColor,
                                ),
                            )
                        }

                        // Dot indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Healthy weight range", color = TacticalColors.TextSecondary, fontSize = 13.sp)
                        Text(
                            text = state.healthyWeightRange,
                            color = TacticalColors.TextPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Copy / Share actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = {
                            val summary = "BMI: ${state.bmiValue} (${state.categoryLabel}), Healthy range: ${state.healthyWeightRange}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("BMI Result", summary))
                            scope.launch { snackbarHostState.showSnackbar("BMI result copied") }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy BMI",
                                tint = TacticalColors.TacticalOrange,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = {
                            val summary = "BMI: ${state.bmiValue} (${state.categoryLabel}), Healthy range: ${state.healthyWeightRange}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, summary)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share BMI Result"))
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share BMI",
                                tint = TacticalColors.TacticalOrange,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "For general information only, not medical advice.",
                color = TacticalColors.TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepperInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .tacticalRecessedWell(cornerRadius = 16.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = TacticalTypography.TelemetryLabel.copy(fontSize = 10.sp))
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Numeric text field
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TacticalTypography.OledReadoutMedium.copy(fontSize = 24.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TacticalColors.TextPrimary,
                        unfocusedTextColor = TacticalColors.TextPrimary,
                        cursorColor = TacticalColors.TacticalOrange,
                    ),
                    modifier = Modifier.weight(1f),
                )

                // Steppers [ - ] [ + ]
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StepperButton(text = "−", onClick = onDecrement)
                    StepperButton(text = "+", onClick = onIncrement)
                }
            }
        }
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalColors.KeycapUtilBase)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TacticalColors.TacticalOrange,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BiometricSpectrumGauge(
    bmiScore: Double,
    currentCategory: String,
) {
    val zones = listOf(
        Color(0xFF38BDF8) to "Underweight",
        TacticalColors.EmeraldTelemetry to "Normal",
        TacticalColors.TacticalAmber to "Overweight",
        Color(0xFFEF4444) to "Obese",
    )

    // Fraction position from BMI 15.0 to 35.0
    val rawFrac = ((bmiScore - 15.0) / 20.0).toFloat().coerceIn(0.04f, 0.96f)
    val animatedFrac by animateFloatAsState(
        targetValue = rawFrac,
        animationSpec = tween(durationMillis = 400),
        label = "GaugePointer",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Pointer Needle Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val pointerX = size.width * animatedFrac
            val path = Path().apply {
                moveTo(pointerX, size.height)
                lineTo(pointerX - 6.dp.toPx(), 0f)
                lineTo(pointerX + 6.dp.toPx(), 0f)
                close()
            }
            drawPath(path, color = TacticalColors.TacticalOrange)
        }

        // Multi-segment 4-zone spectrum bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            zones.forEach { (zoneColor, _) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(zoneColor)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Zone Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            zones.forEach { (zoneColor, label) ->
                val isActive = label.equals(currentCategory, ignoreCase = true)
                Text(
                    text = label,
                    color = if (isActive) zoneColor else TacticalColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SegmentedModeItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isSelected) Modifier.background(TacticalColors.KeycapEqualsBase)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TacticalColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
