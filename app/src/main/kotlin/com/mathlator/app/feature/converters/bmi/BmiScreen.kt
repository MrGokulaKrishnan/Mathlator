package com.mathlator.app.feature.converters.bmi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(
    viewModel: BmiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("BMI", style = TitleTextStyle, color = colors.textMain) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textMain,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Metric / Imperial toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.unitSystem == UnitSystem.METRIC,
                    onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Metric (cm / kg)")
                }
                SegmentedButton(
                    selected = state.unitSystem == UnitSystem.IMPERIAL,
                    onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Imperial (ft+in / lb)")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Inputs
            if (state.unitSystem == UnitSystem.METRIC) {
                BmiInputField(
                    label = "Height (cm)",
                    value = state.heightCm,
                    onValueChange = { viewModel.setHeightCm(it) },
                )
                Spacer(Modifier.height(12.dp))
                BmiInputField(
                    label = "Weight (kg)",
                    value = state.weightKg,
                    onValueChange = { viewModel.setWeightKg(it) },
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BmiInputField(
                        label = "Height (ft)",
                        value = state.heightFt,
                        onValueChange = { viewModel.setHeightFt(it) },
                        modifier = Modifier.weight(1f),
                    )
                    BmiInputField(
                        label = "Height (in)",
                        value = state.heightIn,
                        onValueChange = { viewModel.setHeightIn(it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                BmiInputField(
                    label = "Weight (lb)",
                    value = state.weightLb,
                    onValueChange = { viewModel.setWeightLb(it) },
                )
            }

            Spacer(Modifier.height(16.dp))

            // Asian cut-offs switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Asian cut-offs", color = colors.textMain, fontSize = 16.sp)
                    Text("Overweight ≥ 23, Obese ≥ 25", color = colors.textDim, fontSize = 12.sp)
                }
                Switch(
                    checked = state.isAsianCutoffs,
                    onCheckedChange = { viewModel.setAsianCutoffs(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = colors.surfaceHigh,
                        uncheckedThumbColor = colors.textDim,
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Colored Segmented Bar
            BmiSegmentedBar(currentCategory = state.categoryLabel)

            Spacer(Modifier.height(20.dp))

            // Result Card
            ResultCard(
                headline = "${state.bmiValue} — ${state.categoryLabel}",
                rows = listOf(
                    "Healthy weight range" to state.healthyWeightRange,
                ),
                onCopy = {},
                onShare = {},
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "For general information only, not medical advice.",
                color = colors.textDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun BmiInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colors.textDim) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            cursorColor = Primary,
            unfocusedBorderColor = colors.divider,
            focusedContainerColor = colors.surfaceHigh,
            unfocusedContainerColor = colors.surfaceHigh,
            focusedTextColor = colors.textMain,
            unfocusedTextColor = colors.textMain,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun BmiSegmentedBar(currentCategory: String) {
    val colors = listOf(
        Color(0xFF33B5E5) to "Underweight",
        Color(0xFF00C853) to "Normal",
        Color(0xFFFFB300) to "Overweight",
        Color(0xFFFF4444) to "Obese",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
        ) {
            colors.forEach { (color, _) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            colors.forEach { (_, cat) ->
                Text(
                    text = cat,
                    color = if (cat == currentCategory) Primary else mathlatorColors.textDim,
                    fontSize = 11.sp,
                    fontWeight = if (cat == currentCategory) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
