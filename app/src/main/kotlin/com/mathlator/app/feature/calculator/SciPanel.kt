package com.mathlator.app.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.ui.components.tacticalExtrudedKeycap
import com.mathlator.app.ui.theme.SciKeyTextStyle
import com.mathlator.app.ui.theme.TacticalColors

@Composable
fun SciPanel(
    angleMode: AngleMode,
    isSecondFunction: Boolean,
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun SciKey(
        label: String,
        contentDesc: String,
        keyId: String,
        isHighlighted: Boolean = false,
        modifier: Modifier = Modifier,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Box(
            modifier = modifier
                .height(40.dp)
                .tacticalExtrudedKeycap(
                    cornerRadius = 12.dp,
                    isPressed = isPressed,
                    isAccent = isHighlighted,
                )
                .background(
                    if (isHighlighted) TacticalColors.KeycapEqualsBase
                    else TacticalColors.KeycapUtilBase
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 24.dp),
                    onClick = { onKey(keyId) },
                )
                .semantics { contentDescription = contentDesc },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = SciKeyTextStyle.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isHighlighted) androidx.compose.ui.graphics.Color.White else TacticalColors.TacticalOrange,
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Row 1: ( ) x² xʸ √
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SciKey("(", "open parenthesis", "(", modifier = Modifier.weight(1f))
            SciKey(")", "close parenthesis", ")", modifier = Modifier.weight(1f))
            SciKey("x²", "square", "x²", modifier = Modifier.weight(1f))
            SciKey("xʸ", "power", "^", modifier = Modifier.weight(1f))
            SciKey("√", "square root", "√", modifier = Modifier.weight(1f))
        }

        // Row 2: sin cos tan ln log (or sin⁻¹ cos⁻¹ tan⁻¹ eˣ 10ˣ when 2nd active)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isSecondFunction) {
                SciKey("sin⁻¹", "arc sine", "asin", modifier = Modifier.weight(1f))
                SciKey("cos⁻¹", "arc cosine", "acos", modifier = Modifier.weight(1f))
                SciKey("tan⁻¹", "arc tangent", "atan", modifier = Modifier.weight(1f))
                SciKey("eˣ", "e to the x", "exp", modifier = Modifier.weight(1f))
                SciKey("10ˣ", "10 to the x", "pow10", modifier = Modifier.weight(1f))
            } else {
                SciKey("sin", "sine", "sin", modifier = Modifier.weight(1f))
                SciKey("cos", "cosine", "cos", modifier = Modifier.weight(1f))
                SciKey("tan", "tangent", "tan", modifier = Modifier.weight(1f))
                SciKey("ln", "natural log", "ln", modifier = Modifier.weight(1f))
                SciKey("log", "log base 10", "log", modifier = Modifier.weight(1f))
            }
        }

        // Row 3: π e x! 2nd DEG/RAD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SciKey("π", "pi", "π", modifier = Modifier.weight(1f))
            SciKey("e", "euler's constant", "e", modifier = Modifier.weight(1f))
            SciKey("x!", "factorial", "!", modifier = Modifier.weight(1f))
            SciKey("2nd", "second function", "2nd", isHighlighted = isSecondFunction, modifier = Modifier.weight(1f))
            SciKey(
                label = if (angleMode == AngleMode.DEG) "DEG" else "RAD",
                contentDesc = "angle mode toggle, currently ${angleMode.name}",
                keyId = "DEG_RAD",
                modifier = Modifier.weight(1f),
            )
        }
    }
}
