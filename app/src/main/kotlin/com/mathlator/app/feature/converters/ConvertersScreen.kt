package com.mathlator.app.feature.converters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.mathlator.app.ui.components.TacticalIcons
import com.mathlator.app.ui.components.tacticalCard
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.navigation.Screen
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography

data class BentoToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun ConvertersScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Strict order per Section 7:
    // Age, Area, BMI, Data, Date, Discount, Length, Mass, Numeral system, Speed, Temperature, Time, Volume
    val tools = listOf(
        BentoToolItem("Age", "yr • mo • day", TacticalIcons.Age, Screen.AgeConverter.route),
        BentoToolItem("Area", "m² • ac • ha", TacticalIcons.Area, Screen.UnitConverter.route("area")),
        BentoToolItem("BMI", "kg/m² • index", TacticalIcons.Bmi, Screen.BmiConverter.route),
        BentoToolItem("Data", "MB • GiB • TB", TacticalIcons.Data, Screen.UnitConverter.route("data")),
        BentoToolItem("Date", "diff • add/sub", TacticalIcons.Date, Screen.DateConverter.route),
        BentoToolItem("Discount", "off • tax", TacticalIcons.Discount, Screen.DiscountConverter.route),
        BentoToolItem("Length", "m • ft • in", TacticalIcons.Length, Screen.UnitConverter.route("length")),
        BentoToolItem("Mass", "kg • lb • oz", TacticalIcons.Mass, Screen.UnitConverter.route("mass")),
        BentoToolItem("Numeral", "HEX • DEC", TacticalIcons.Numeral, Screen.NumeralConverter.route),
        BentoToolItem("Speed", "km/h • mph", TacticalIcons.Speed, Screen.UnitConverter.route("speed")),
        BentoToolItem("Temperature", "°C • °F • K", TacticalIcons.Temperature, Screen.UnitConverter.route("temperature")),
        BentoToolItem("Time", "hr • min • s", TacticalIcons.Time, Screen.UnitConverter.route("time")),
        BentoToolItem("Volume", "L • gal • m³", TacticalIcons.Volume, Screen.UnitConverter.route("volume")),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(TacticalDimens.BentoCardSpacing),
        horizontalArrangement = Arrangement.spacedBy(TacticalDimens.BentoCardSpacing),
    ) {
        items(tools, key = { it.title }) { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .height(118.dp)
                    .tacticalCard(
                        cornerRadius = TacticalDimens.CardCornerRadius,
                        isPressed = isPressed,
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 48.dp),
                        onClick = { onNavigateToRoute(item.route) },
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp)
                    .semantics { contentDescription = "${item.title} converter" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Etched Icon Socket
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .tacticalSocket(cornerRadius = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = TacticalColors.TacticalOrange,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        style = TacticalTypography.BentoTitle,
                        maxLines = 1,
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = item.subtitle,
                        style = TacticalTypography.BentoSubtitle,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
