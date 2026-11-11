package com.mathlator.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography

@Composable
fun ResultCard(
    headline: String,
    rows: List<Pair<String, String>>,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .tacticalRecessedWell(
                cornerRadius = TacticalDimens.WellCornerRadius,
                isFocused = true,
            )
            .padding(18.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TELEMETRY RESULT",
                style = TacticalTypography.TelemetryMicroBadge,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = headline,
                style = TacticalTypography.OledReadoutMedium.copy(
                    fontSize = 22.sp,
                    color = TacticalColors.TacticalOrange,
                ),
            )
            Spacer(Modifier.height(14.dp))
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        color = TacticalColors.TextSecondary,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = value,
                        color = TacticalColors.TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = TacticalColors.TacticalOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = TacticalColors.TacticalOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
