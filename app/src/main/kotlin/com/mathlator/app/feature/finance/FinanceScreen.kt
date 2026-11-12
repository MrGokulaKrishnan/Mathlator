package com.mathlator.app.feature.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.finance.GstCalculator
import com.mathlator.app.core.format.NumberFormatter
import com.mathlator.app.ui.components.TacticalIcons
import com.mathlator.app.ui.components.tacticalCard
import com.mathlator.app.ui.components.tacticalExtrudedKeycap
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.navigation.Screen
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import java.math.BigDecimal

data class FinanceInstrumentItem(
    val title: String,
    val subtitle: String,
    val badge: String,
    val isLiveFx: Boolean,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun FinanceScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val instruments = listOf(
        FinanceInstrumentItem(
            title = "GST Tax",
            subtitle = "CGST / SGST Split",
            badge = "5–28%",
            isLiveFx = false,
            icon = TacticalIcons.Gst,
            route = Screen.GstTool.route,
        ),
        FinanceInstrumentItem(
            title = "Currency",
            subtitle = "AED • USD • INR →",
            badge = "FX",
            isLiveFx = true,
            icon = TacticalIcons.Currency,
            route = Screen.CurrencyTool.route,
        ),
        FinanceInstrumentItem(
            title = "Investment",
            subtitle = "SIP & Compound",
            badge = "CAGR",
            isLiveFx = false,
            icon = TacticalIcons.Investment,
            route = Screen.InvestmentTool.route,
        ),
        FinanceInstrumentItem(
            title = "Loan EMI",
            subtitle = "Monthly Breakdown",
            badge = "EMI",
            isLiveFx = false,
            icon = TacticalIcons.Loan,
            route = Screen.LoanTool.route,
        ),
    )

    var gstBaseAmount by remember { mutableStateOf("10000") }
    var selectedSlab by remember { mutableStateOf("18") }

    val gstResult = remember(gstBaseAmount, selectedSlab) {
        val amount = gstBaseAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = selectedSlab.toBigDecimalOrNull() ?: BigDecimal("18")
        GstCalculator.addGst(amount, rate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        // ── 2×2 Tactical Instrument Cards Grid ────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InstrumentCard(
                item = instruments[0],
                onClick = { onNavigateToRoute(instruments[0].route) },
                modifier = Modifier.weight(1f),
            )
            InstrumentCard(
                item = instruments[1],
                onClick = { onNavigateToRoute(instruments[1].route) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InstrumentCard(
                item = instruments[2],
                onClick = { onNavigateToRoute(instruments[2].route) },
                modifier = Modifier.weight(1f),
            )
            InstrumentCard(
                item = instruments[3],
                onClick = { onNavigateToRoute(instruments[3].route) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Embedded Instant GST Calculator Module ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tacticalRecessedWell(
                    cornerRadius = TacticalDimens.WellCornerRadius,
                    isFocused = true,
                )
                .padding(14.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top header: Title + Slab Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "INSTANT GST CALCULATOR",
                        style = TacticalTypography.TelemetryMicroBadge.copy(
                            color = TacticalColors.TacticalOrange,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("5", "12", "18", "28").forEach { slab ->
                            val isSelected = selectedSlab == slab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) TacticalColors.KeycapEqualsBase
                                        else TacticalColors.KeycapUtilBase
                                    )
                                    .clickable { selectedSlab = slab }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$slab%",
                                    color = if (isSelected) Color.White else TacticalColors.TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Presets quick row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("1000", "5000", "10000", "25000").forEach { preset ->
                        val isSelected = gstBaseAmount == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) TacticalColors.KeycapEqualsBase
                                    else TacticalColors.KeycapNumBase
                                )
                                .clickable { gstBaseAmount = preset },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "₹$preset",
                                color = if (isSelected) Color.White else TacticalColors.TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Dual Cards: Base Amount & GST Tax
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tacticalCard(cornerRadius = 12.dp)
                            .padding(10.dp),
                    ) {
                        Column {
                            Text(
                                text = "BASE AMOUNT (₹)",
                                style = TacticalTypography.TelemetryLabel.copy(fontSize = 9.sp),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "₹${NumberFormatter.format(gstBaseAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO)}",
                                style = TacticalTypography.TelemetryValue.copy(
                                    fontSize = 16.sp,
                                    color = TacticalColors.TextPrimary,
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tacticalCard(cornerRadius = 12.dp)
                            .padding(10.dp),
                    ) {
                        Column {
                            Text(
                                text = "GST TAX ($selectedSlab%)",
                                style = TacticalTypography.TelemetryLabel.copy(fontSize = 9.sp),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "+₹${NumberFormatter.format(gstResult.gstAmount)}",
                                style = TacticalTypography.TelemetryValue.copy(
                                    fontSize = 16.sp,
                                    color = TacticalColors.TacticalOrange,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Bottom summary bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "TOTAL INCLUSIVE:",
                            style = TacticalTypography.TelemetryLabel.copy(fontSize = 10.sp),
                        )
                        Text(
                            text = "₹${NumberFormatter.format(gstResult.totalAmount)}",
                            style = TacticalTypography.TelemetryValue.copy(
                                fontSize = 16.sp,
                                color = TacticalColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToRoute(Screen.GstTool.route) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Full Tool",
                            color = TacticalColors.TacticalOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Open full tool",
                            tint = TacticalColors.TacticalOrange,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InstrumentCard(
    item: FinanceInstrumentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(116.dp)
            .tacticalCard(
                cornerRadius = TacticalDimens.CardCornerRadius,
                isPressed = isPressed,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 54.dp),
                onClick = onClick,
            )
            .padding(12.dp)
            .semantics { contentDescription = item.title },
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Etched Icon Socket
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .tacticalSocket(cornerRadius = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = TacticalColors.TacticalOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.isLiveFx) Color(0x2A10B981)
                            else Color(0x26FF5E00)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        if (item.isLiveFx) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFF10B981), shape = androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                        Text(
                            text = item.badge,
                            color = if (item.isLiveFx) Color(0xFF34D399) else TacticalColors.TacticalOrange,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Column {
                Text(
                    text = item.title,
                    style = TacticalTypography.BentoTitle.copy(fontSize = 14.sp),
                    maxLines = 1,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = item.subtitle,
                    style = TacticalTypography.BentoSubtitle.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (item.isLiveFx) TacticalColors.TacticalOrange else TacticalColors.TextMuted,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun MiniTelemetryBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalColors.SurfaceSocket)
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column {
            Text(label, color = TacticalColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(value, color = TacticalColors.TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}
