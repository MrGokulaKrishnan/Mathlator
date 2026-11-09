package com.mathlator.app.feature.converters.numeral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumeralScreen(
    viewModel: NumeralViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()

    val enabledDigits = when (state.activeRadix) {
        Radix.BIN -> setOf('0', '1')
        Radix.OCT -> ('0'..'7').toSet()
        Radix.DEC -> ('0'..'9').toSet()
        Radix.HEX -> (('0'..'9') + ('A'..'F')).toSet()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Numeral system", style = TitleTextStyle, color = colors.textMain) },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Integers only, up to 128 bits",
                color = colors.textDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Four radix rows: BIN, OCT, DEC, HEX
            RadixRow(
                label = "HEX",
                value = state.hexValue,
                isActive = state.activeRadix == Radix.HEX,
                onClick = { viewModel.selectRadix(Radix.HEX) },
            )
            Spacer(Modifier.height(8.dp))

            RadixRow(
                label = "DEC",
                value = state.decValue,
                isActive = state.activeRadix == Radix.DEC,
                onClick = { viewModel.selectRadix(Radix.DEC) },
            )
            Spacer(Modifier.height(8.dp))

            RadixRow(
                label = "OCT",
                value = state.octValue,
                isActive = state.activeRadix == Radix.OCT,
                onClick = { viewModel.selectRadix(Radix.OCT) },
            )
            Spacer(Modifier.height(8.dp))

            RadixRow(
                label = "BIN",
                value = state.binValue,
                isActive = state.activeRadix == Radix.BIN,
                onClick = { viewModel.selectRadix(Radix.BIN) },
            )

            Spacer(Modifier.height(16.dp))

            // Numeral Keypad: 4 columns
            // Row A-B-C-D (only if HEX active)
            if (state.activeRadix == Radix.HEX) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A", "B", "C", "D").forEach { hexChar ->
                        KeypadBtn(
                            label = hexChar,
                            enabled = true,
                            onClick = { viewModel.onChar(hexChar[0]) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("E", "F").forEach { hexChar ->
                        KeypadBtn(
                            label = hexChar,
                            enabled = true,
                            onClick = { viewModel.onChar(hexChar[0]) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.weight(2f))
                }
                Spacer(Modifier.height(8.dp))
            }

            // Standard numeric keypad rows: 7 8 9, 4 5 6, 1 2 3, ± 0 ⌫ C
            val rows = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { d ->
                        KeypadBtn(
                            label = d,
                            enabled = enabledDigits.contains(d[0]),
                            onClick = { viewModel.onChar(d[0]) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row[0] == "1") {
                        KeypadBtn(
                            label = "⌫",
                            enabled = true,
                            onClick = { viewModel.onDelete() },
                            textColor = colors.operator,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Bottom row: ±, 0, C
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeypadBtn(
                    label = "±",
                    enabled = true,
                    onClick = { viewModel.onSign() },
                    modifier = Modifier.weight(1f),
                )
                KeypadBtn(
                    label = "0",
                    enabled = enabledDigits.contains('0'),
                    onClick = { viewModel.onChar('0') },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.weight(1f))
                KeypadBtn(
                    label = "C",
                    enabled = true,
                    onClick = { viewModel.onClear() },
                    textColor = colors.operator,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RadixRow(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val colors = mathlatorColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = if (isActive) BorderStroke(1.5.dp, brush = brandGradientBrush()) else BorderStroke(1.dp, colors.divider),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = if (isActive) Primary else colors.textDim,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value.ifEmpty { "0" },
                color = colors.textMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun KeypadBtn(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    textColor: Color = mathlatorColors.textMain,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceHigh,
            contentColor = textColor,
            disabledContainerColor = colors.surfaceHigh.copy(alpha = 0.25f),
            disabledContentColor = textColor.copy(alpha = 0.25f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Normal)
    }
}
