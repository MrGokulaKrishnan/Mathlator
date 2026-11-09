package com.mathlator.app.feature.finance.loan

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.components.DonutChart
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.DonutSecondary
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(
    viewModel: LoanViewModel,
    onNavigateBack: () -> Unit,
    onViewAmortization: (amount: String, rate: String, months: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Loan", style = TitleTextStyle, color = colors.textMain) },
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
            // Loan Amount
            LoanInputField(
                label = "Loan amount",
                value = state.amountInput,
                onValueChange = { viewModel.setAmount(it) },
                prefix = "₹ ",
            )

            Spacer(Modifier.height(12.dp))

            // Interest Rate
            LoanInputField(
                label = "Interest rate (% p.a.)",
                value = state.interestRateInput,
                onValueChange = { viewModel.setInterestRate(it) },
            )

            Spacer(Modifier.height(12.dp))

            // Tenure + Years/Months Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoanInputField(
                    label = "Tenure",
                    value = state.tenureInput,
                    onValueChange = { viewModel.setTenure(it) },
                    modifier = Modifier.weight(1f),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = state.tenureUnit == TenureUnit.YEARS,
                        onClick = { viewModel.setTenureUnit(TenureUnit.YEARS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Primary,
                            activeContentColor = Color.White,
                            inactiveContainerColor = colors.surfaceHigh,
                            inactiveContentColor = colors.textMain,
                        ),
                    ) {
                        Text("Years")
                    }
                    SegmentedButton(
                        selected = state.tenureUnit == TenureUnit.MONTHS,
                        onClick = { viewModel.setTenureUnit(TenureUnit.MONTHS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Primary,
                            activeContentColor = Color.White,
                            inactiveContainerColor = colors.surfaceHigh,
                            inactiveContentColor = colors.textMain,
                        ),
                    ) {
                        Text("Months")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Headline EMI + Result Card
            val r = state.result
            val headline = "₹${NumberFormatter.formatMoney(r.emi)}"
            val rows = listOf(
                "Total interest" to "₹${NumberFormatter.formatMoney(r.totalInterest)}",
                "Total payment" to "₹${NumberFormatter.formatMoney(r.totalPayment)}",
            )

            ResultCard(
                headline = "Monthly EMI: $headline",
                rows = rows,
                onCopy = {},
                onShare = {},
            )

            Spacer(Modifier.height(24.dp))

            // Donut Chart + Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DonutChart(
                    principalRatio = state.principalRatio,
                    interestRatio = state.interestRatio,
                    size = 140.dp,
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = Primary,
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Principal", color = colors.textDim, fontSize = 12.sp)
                            Text(
                                "₹${NumberFormatter.formatMoney(state.amountInput.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO)}",
                                color = colors.textMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = DonutSecondary,
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Interest", color = colors.textDim, fontSize = 12.sp)
                            Text(
                                "₹${NumberFormatter.formatMoney(r.totalInterest)}",
                                color = colors.textMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // View Amortization Schedule Button (brand gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(brandGradientBrush(), RoundedCornerShape(28.dp)),
            ) {
                Button(
                    onClick = {
                        onViewAmortization(state.amountInput, state.interestRateInput, state.totalMonths)
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        "View amortization schedule",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colors.textDim) },
        prefix = if (prefix != null) { { Text(prefix, color = Primary) } } else null,
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
