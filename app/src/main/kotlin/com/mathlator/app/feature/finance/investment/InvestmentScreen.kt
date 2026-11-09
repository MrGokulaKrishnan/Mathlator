package com.mathlator.app.feature.finance.investment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.finance.CompoundingFrequency
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.components.LineChart
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InvestmentScreen(
    viewModel: InvestmentViewModel,
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
                title = { Text("Investment", style = TitleTextStyle, color = colors.textMain) },
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
                .verticalScroll(rememberScrollState()),
        ) {
            // Three tabs: SIP | Lump sum | FD
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = colors.background,
                contentColor = Primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab.ordinal]),
                        color = Primary,
                    )
                },
            ) {
                Tab(
                    selected = state.selectedTab == InvestmentTab.SIP,
                    onClick = { viewModel.selectTab(InvestmentTab.SIP) },
                    text = { Text("SIP", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                    selectedContentColor = Primary,
                    unselectedContentColor = colors.textDim,
                )
                Tab(
                    selected = state.selectedTab == InvestmentTab.LUMPSUM,
                    onClick = { viewModel.selectTab(InvestmentTab.LUMPSUM) },
                    text = { Text("Lump sum", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                    selectedContentColor = Primary,
                    unselectedContentColor = colors.textDim,
                )
                Tab(
                    selected = state.selectedTab == InvestmentTab.FD,
                    onClick = { viewModel.selectTab(InvestmentTab.FD) },
                    text = { Text("FD", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                    selectedContentColor = Primary,
                    unselectedContentColor = colors.textDim,
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (state.selectedTab) {
                    InvestmentTab.SIP -> {
                        InvestInput(
                            label = "Monthly amount",
                            value = state.sipMonthlyAmount,
                            onValueChange = { viewModel.setSipMonthlyAmount(it) },
                        )
                        Spacer(Modifier.height(16.dp))

                        SliderLabelValue("Expected return", "${state.sipReturnRatePct.roundToInt()}% p.a.")
                        Slider(
                            value = state.sipReturnRatePct,
                            onValueChange = { viewModel.setSipReturnRate(it) },
                            valueRange = 1f..30f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )

                        Spacer(Modifier.height(8.dp))
                        SliderLabelValue("Time period", "${state.sipYears} years")
                        Slider(
                            value = state.sipYears.toFloat(),
                            onValueChange = { viewModel.setSipYears(it.toInt()) },
                            valueRange = 1f..40f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )

                        Spacer(Modifier.height(8.dp))
                        SliderLabelValue("Annual step-up", "${state.sipStepUpPct.roundToInt()}%")
                        Slider(
                            value = state.sipStepUpPct,
                            onValueChange = { viewModel.setSipStepUp(it) },
                            valueRange = 0f..20f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )
                    }
                    InvestmentTab.LUMPSUM -> {
                        InvestInput(
                            label = "Principal amount",
                            value = state.lumpSumPrincipal,
                            onValueChange = { viewModel.setLumpSumPrincipal(it) },
                        )
                        Spacer(Modifier.height(16.dp))

                        SliderLabelValue("Expected return", "${state.lumpSumReturnRatePct.roundToInt()}% p.a.")
                        Slider(
                            value = state.lumpSumReturnRatePct,
                            onValueChange = { viewModel.setLumpSumReturnRate(it) },
                            valueRange = 1f..30f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )

                        Spacer(Modifier.height(8.dp))
                        SliderLabelValue("Time period", "${state.lumpSumYears} years")
                        Slider(
                            value = state.lumpSumYears.toFloat(),
                            onValueChange = { viewModel.setLumpSumYears(it.toInt()) },
                            valueRange = 1f..40f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )
                    }
                    InvestmentTab.FD -> {
                        InvestInput(
                            label = "Principal amount",
                            value = state.fdPrincipal,
                            onValueChange = { viewModel.setFdPrincipal(it) },
                        )
                        Spacer(Modifier.height(16.dp))

                        SliderLabelValue("Interest rate", "${state.fdReturnRatePct.roundToInt()}% p.a.")
                        Slider(
                            value = state.fdReturnRatePct,
                            onValueChange = { viewModel.setFdReturnRate(it) },
                            valueRange = 1f..20f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )

                        Spacer(Modifier.height(8.dp))
                        SliderLabelValue("Time period", "${state.fdYears} years")
                        Slider(
                            value = state.fdYears.toFloat(),
                            onValueChange = { viewModel.setFdYears(it.toInt()) },
                            valueRange = 1f..25f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("Compounding frequency", color = colors.textDim, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompoundingFrequency.entries.forEach { freq ->
                                FilterChip(
                                    selected = state.fdFrequency == freq,
                                    onClick = { viewModel.setFdFrequency(freq) },
                                    label = { Text(freq.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Result Card
                val r = state.result
                val headline = "Total: ₹${NumberFormatter.formatMoney(r.totalValue)}"
                val rows = mutableListOf(
                    "Invested amount" to "₹${NumberFormatter.formatMoney(r.invested)}",
                    "Estimated returns" to "₹${NumberFormatter.formatMoney(r.estimatedReturn)}",
                )
                if (state.selectedTab == InvestmentTab.FD) {
                    rows.add("Effective annual yield" to state.fdEffectiveYield)
                }

                ResultCard(
                    headline = headline,
                    rows = rows,
                    onCopy = {},
                    onShare = {},
                )

                Spacer(Modifier.height(20.dp))

                // Line / Area Chart
                LineChart(
                    investedPoints = state.investedSeries,
                    valuePoints = state.valueSeries,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Estimates only. Not financial advice.",
                    color = colors.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InvestInput(label: String, value: String, onValueChange: (String) -> Unit) {
    val colors = mathlatorColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colors.textDim) },
        prefix = { Text("₹ ", color = Primary) },
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
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SliderLabelValue(label: String, value: String) {
    val colors = mathlatorColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colors.textDim, fontSize = 14.sp)
        Text(value, color = colors.textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
