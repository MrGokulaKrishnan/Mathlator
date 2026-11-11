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
    val previewTag: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun FinanceScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val instruments = listOf(
        FinanceInstrumentItem("GST Calculator", "5%–28% Slabs", TacticalIcons.Gst, Screen.GstTool.route),
        FinanceInstrumentItem("Currency Rack", "AED • USD • INR", TacticalIcons.Currency, Screen.CurrencyTool.route),
        FinanceInstrumentItem("Investment", "CAGR / SIP Growth", TacticalIcons.Investment, Screen.InvestmentTool.route),
        FinanceInstrumentItem("Loan & EMI", "EMI / Amortization", TacticalIcons.Loan, Screen.LoanTool.route),
    )

    var gstBaseAmount by remember { mutableStateOf("1000") }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // ── 2×2 Tactical Instrument Cards Grid ────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        Spacer(Modifier.height(18.dp))

        // ── Embedded Quick Interactive GST Split Module ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tacticalRecessedWell(
                    cornerRadius = TacticalDimens.WellCornerRadius,
                    isFocused = true,
                )
                .padding(18.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "QUICK GST SPLIT // TELEMETRY",
                        style = TacticalTypography.TelemetryMicroBadge,
                    )
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

                Spacer(Modifier.height(12.dp))

                // Amount Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("500", "1000", "5000", "25000").forEach { preset ->
                        val isSelected = gstBaseAmount == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TacticalColors.KeycapEqualsBase else TacticalColors.KeycapNumBase)
                                .clickable { gstBaseAmount = preset },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "₹$preset",
                                color = if (isSelected) Color.White else TacticalColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Slab Selector Pills [5%] [12%] [18%] [28%]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("5", "12", "18", "28").forEach { slab ->
                        val isSelected = selectedSlab == slab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TacticalColors.KeycapEqualsBase else TacticalColors.KeycapUtilBase)
                                .clickable { selectedSlab = slab },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$slab%",
                                color = if (isSelected) Color.White else TacticalColors.TacticalOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Breakdown Matrix (CGST + SGST + Total)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MiniTelemetryBox(
                        label = "CGST (${selectedSlab.toInt() / 2}%)",
                        value = "₹${NumberFormatter.format(gstResult.cgst)}",
                        modifier = Modifier.weight(1f),
                    )
                    MiniTelemetryBox(
                        label = "SGST (${selectedSlab.toInt() / 2}%)",
                        value = "₹${NumberFormatter.format(gstResult.sgst)}",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "TOTAL INVOICE (INC. GST)",
                            style = TacticalTypography.TelemetryLabel,
                        )
                        Text(
                            text = "₹${NumberFormatter.format(gstResult.totalAmount)}",
                            style = TacticalTypography.TelemetryValue.copy(
                                color = TacticalColors.TacticalOrange,
                                fontSize = 24.sp,
                            ),
                        )
                    }
                    Text(
                        text = "+₹${NumberFormatter.format(gstResult.gstAmount)} GST",
                        color = TacticalColors.TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    )
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
            .height(130.dp)
            .tacticalCard(
                cornerRadius = TacticalDimens.CardCornerRadius,
                isPressed = isPressed,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 54.dp),
                onClick = onClick,
            )
            .padding(14.dp)
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
                        .size(40.dp)
                        .tacticalSocket(cornerRadius = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = TacticalColors.TacticalOrange,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = TacticalColors.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }

            Column {
                Text(
                    text = item.title,
                    style = TacticalTypography.BentoTitle.copy(fontSize = 14.sp),
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.previewTag,
                    style = TacticalTypography.BentoSubtitle,
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
