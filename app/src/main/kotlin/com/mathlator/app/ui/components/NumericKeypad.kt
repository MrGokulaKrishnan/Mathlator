package com.mathlator.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.mathlatorColors

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onSign: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    enabledDigits: Set<Char>? = null,
    showDecimal: Boolean = true,
    showSign: Boolean = true,
) {
    val colors = mathlatorColors

    @Composable
    fun RowScope.NumKey(
        label: String,
        onClick: () -> Unit,
        enabled: Boolean = true,
        keyColor: Color = colors.textMain,
        weight: Float = 1f,
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .weight(weight)
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surfaceHigh,
                contentColor = keyColor,
                disabledContainerColor = colors.surfaceHigh.copy(alpha = 0.3f),
                disabledContentColor = keyColor.copy(alpha = 0.3f),
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Normal)
        }
    }

    fun isEnabled(digit: Char) = enabledDigits == null || digit in enabledDigits

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Rows 7-8-9, 4-5-6, 1-2-3 with ⌫ on the last row
        listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3")).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { d -> NumKey(d, { onDigit(d) }, isEnabled(d[0])) }
                if (row[0] == "1") {
                    NumKey("⌫", onDelete, keyColor = colors.operator)
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // Bottom row: . / 0 / ± / C
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showDecimal) NumKey(".", onDecimal) else Spacer(Modifier.weight(1f))
            NumKey("0", { onDigit("0") })
            if (showSign) NumKey("±", onSign) else Spacer(Modifier.weight(1f))
            NumKey("C", onClear, keyColor = colors.operator)
        }
    }
}
