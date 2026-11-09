package com.mathlator.app.feature.finance.loan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AmortizationScreen(
    viewModel: AmortizationViewModel,
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
                title = { Text("Amortization", style = TitleTextStyle, color = colors.textMain) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textMain,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shareCsv() }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share as CSV",
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
                .padding(innerPadding),
        ) {
            // Month / Year Toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = state.viewMode == AmortizationViewMode.MONTHLY,
                    onClick = { viewModel.setViewMode(AmortizationViewMode.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Monthly")
                }
                SegmentedButton(
                    selected = state.viewMode == AmortizationViewMode.YEARLY,
                    onClick = { viewModel.setViewMode(AmortizationViewMode.YEARLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Yearly")
                }
            }

            // Sticky Header + LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceHigh)
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (state.viewMode == AmortizationViewMode.MONTHLY) "Mo" else "Yr",
                            color = Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f),
                        )
                        Text(
                            text = "EMI",
                            color = colors.textDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.3f),
                        )
                        Text(
                            text = "Principal",
                            color = colors.textDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.3f),
                        )
                        Text(
                            text = "Interest",
                            color = colors.textDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.3f),
                        )
                        Text(
                            text = "Balance",
                            color = colors.textDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                }

                if (state.viewMode == AmortizationViewMode.MONTHLY) {
                    items(state.monthlySchedule, key = { it.month }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${entry.month}", color = colors.textDim, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text(NumberFormatter.formatMoney(entry.emi), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.principal), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.interest), color = colors.operator, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.balance), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }
                } else {
                    items(state.yearlySchedule, key = { it.year }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Y${entry.year}", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.7f))
                            Text(NumberFormatter.formatMoney(entry.emi), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.principal), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.interest), color = colors.operator, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                            Text(NumberFormatter.formatMoney(entry.endingBalance), color = colors.textMain, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
