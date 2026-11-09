package com.mathlator.app.feature.finance.gst

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GstScreen(
    viewModel: GstViewModel,
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
                title = { Text("GST", style = TitleTextStyle, color = colors.textMain) },
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
            // Segmented control: Add GST | Remove GST
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.isAddGst,
                    onClick = { viewModel.setIsAddGst(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Add GST")
                }
                SegmentedButton(
                    selected = !state.isAddGst,
                    onClick = { viewModel.setIsAddGst(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = colors.surfaceHigh,
                        inactiveContentColor = colors.textMain,
                    ),
                ) {
                    Text("Remove GST")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { viewModel.setAmountInput(it) },
                label = { Text("Amount", color = colors.textDim) },
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

            Spacer(Modifier.height(16.dp))

            Text("GST Rate", color = colors.textMain, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            // Rate Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.availableRates.forEach { rate ->
                    FilterChip(
                        selected = !state.isCustomRate && state.selectedRate == rate,
                        onClick = { viewModel.selectRate(rate) },
                        label = { Text("$rate%") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
                FilterChip(
                    selected = state.isCustomRate,
                    onClick = { viewModel.selectCustomRate() },
                    label = { Text("Custom") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }

            if (state.isCustomRate) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.customRateInput,
                    onValueChange = { viewModel.setCustomRateInput(it) },
                    label = { Text("Custom Rate (%)", color = colors.textDim) },
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

            Spacer(Modifier.height(16.dp))

            // IGST Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("IGST (Inter-state)", color = colors.textMain, fontSize = 16.sp)
                    Text("Single tax instead of CGST + SGST split", color = colors.textDim, fontSize = 12.sp)
                }
                Switch(
                    checked = state.isIgst,
                    onCheckedChange = { viewModel.setIgst(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = colors.surfaceHigh,
                        uncheckedThumbColor = colors.textDim,
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Result Card
            val r = state.result
            val headline = if (state.isAddGst) {
                "Total: ₹${NumberFormatter.formatMoney(r.totalAmount)}"
            } else {
                "Base: ₹${NumberFormatter.formatMoney(r.baseAmount)}"
            }

            val rows = mutableListOf(
                "GST amount" to "₹${NumberFormatter.formatMoney(r.gstAmount)}",
            )
            if (state.isAddGst) {
                rows.add("Base amount" to "₹${NumberFormatter.formatMoney(r.baseAmount)}")
            } else {
                rows.add("Total with GST" to "₹${NumberFormatter.formatMoney(r.totalAmount)}")
            }

            if (state.isIgst) {
                rows.add("IGST" to "₹${NumberFormatter.formatMoney(r.gstAmount)}")
            } else {
                rows.add("CGST" to "₹${NumberFormatter.formatMoney(r.cgst)}")
                rows.add("SGST" to "₹${NumberFormatter.formatMoney(r.sgst)}")
            }

            ResultCard(
                headline = headline,
                rows = rows,
                onCopy = {},
                onShare = {},
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "GST slabs are set by the GST Council and can change, so verify before release.",
                color = colors.textDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
