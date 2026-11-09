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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.ViewInAr
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

data class ConverterToolItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun ConvertersScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors

    // Strict order per Section 7:
    // Age, Area, BMI, Data, Date, Discount, Length, Mass, Numeral system, Speed, Temperature, Time, Volume
    val tools = listOf(
        ConverterToolItem("Age", Icons.Outlined.Person, Screen.AgeConverter.route),
        ConverterToolItem("Area", Icons.Outlined.CropFree, Screen.UnitConverter.route("area")),
        ConverterToolItem("BMI", Icons.Outlined.FitnessCenter, Screen.BmiConverter.route),
        ConverterToolItem("Data", Icons.Outlined.DeviceHub, Screen.UnitConverter.route("data")),
        ConverterToolItem("Date", Icons.Outlined.CalendarMonth, Screen.DateConverter.route),
        ConverterToolItem("Discount", Icons.Outlined.Discount, Screen.DiscountConverter.route),
        ConverterToolItem("Length", Icons.Outlined.Straighten, Screen.UnitConverter.route("length")),
        ConverterToolItem("Mass", Icons.Outlined.Scale, Screen.UnitConverter.route("mass")),
        ConverterToolItem("Numeral system", Icons.Outlined.Numbers, Screen.NumeralConverter.route),
        ConverterToolItem("Speed", Icons.Outlined.Speed, Screen.UnitConverter.route("speed")),
        ConverterToolItem("Temperature", Icons.Outlined.Thermostat, Screen.UnitConverter.route("temperature")),
        ConverterToolItem("Time", Icons.Outlined.AccessTime, Screen.UnitConverter.route("time")),
        ConverterToolItem("Volume", Icons.Outlined.ViewInAr, Screen.UnitConverter.route("volume")),
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
