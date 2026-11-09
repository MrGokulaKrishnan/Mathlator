package com.mathlator.app.feature.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.navigation.Screen
import com.mathlator.app.ui.theme.GridLabelTextStyle
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.mathlatorColors

data class FinanceToolItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun FinanceScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors

    // 4 Finance tools per Section 8: GST, Currency, Investment, Loan
    val tools = listOf(
        FinanceToolItem("GST", Icons.AutoMirrored.Outlined.ReceiptLong, Screen.GstTool.route),
        FinanceToolItem("Currency", Icons.Outlined.CurrencyExchange, Screen.CurrencyTool.route),
        FinanceToolItem("Investment", Icons.AutoMirrored.Outlined.TrendingUp, Screen.InvestmentTool.route),
        FinanceToolItem("Loan", Icons.Outlined.AccountBalance, Screen.LoanTool.route),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tools, key = { it.title }) { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val activeColor = if (isPressed) Primary else colors.textDim
            val labelColor = if (isPressed) Primary else colors.textMain

            Box(
                modifier = Modifier
                    .height(120.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 48.dp),
                        onClick = { onNavigateToRoute(item.route) },
                    )
                    .semantics { contentDescription = item.title },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = item.title,
                        style = GridLabelTextStyle.copy(fontSize = 15.sp),
                        color = labelColor,
                    )
                }
            }
        }
    }
}
