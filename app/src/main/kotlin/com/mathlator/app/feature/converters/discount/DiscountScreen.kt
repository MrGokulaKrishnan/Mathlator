package com.mathlator.app.feature.converters.discount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountScreen(
    viewModel: DiscountViewModel,
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
                title = { Text("Discount", style = TitleTextStyle, color = colors.textMain) },
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
            // Reverse mode switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Reverse calculation", color = colors.textMain, fontSize = 16.sp)
                    Text("Find original price from discounted price", color = colors.textDim, fontSize = 12.sp)
                }
                Switch(
                    checked = state.isReverse,
                    onCheckedChange = { viewModel.setReverse(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = colors.surfaceHigh,
                        uncheckedThumbColor = colors.textDim,
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Inputs
            DiscountInputField(
                label = if (state.isReverse) "Final price" else "Original price",
                value = state.priceInput,
                onValueChange = { viewModel.setPriceInput(it) },
            )

            Spacer(Modifier.height(12.dp))

            DiscountInputField(
                label = "Discount (%)",
                value = state.discountPct,
                onValueChange = { viewModel.setDiscountPct(it) },
            )

            if (!state.isReverse) {
                Spacer(Modifier.height(12.dp))
                DiscountInputField(
                    label = "Extra discount (%) (optional)",
                    value = state.extraDiscountPct,
                    onValueChange = { viewModel.setExtraDiscountPct(it) },
                )
            }

            Spacer(Modifier.height(12.dp))

            DiscountInputField(
                label = "Tax (%) (optional)",
                value = state.taxPct,
                onValueChange = { viewModel.setTaxPct(it) },
            )

            Spacer(Modifier.height(24.dp))

            // Result Card
            val r = state.result
            val headline = if (state.isReverse) {
                "Original price: ${NumberFormatter.formatMoney(r.finalPrice)}"
            } else {
                "Final price: ${NumberFormatter.formatMoney(r.finalPrice)}"
            }

            val rows = listOf(
                "You save" to NumberFormatter.formatMoney(r.youSave),
                "Effective discount" to "${r.effectiveDiscount}%",
            )

            ResultCard(
                headline = headline,
                rows = rows,
                onCopy = {},
                onShare = {},
            )
        }
    }
}

@Composable
private fun DiscountInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
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
        modifier = Modifier.fillMaxWidth(),
    )
}
