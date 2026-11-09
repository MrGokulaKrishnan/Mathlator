package com.mathlator.app.feature.converters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.NumericKeypad
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 300),
        label = "SwapRotation",
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.category.name,
                        style = TitleTextStyle,
                        color = colors.textMain,
                    )
                },
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
            // Data converter: Decimal / Binary toggle
            if (state.category == ConverterCategory.Data) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    FilterChip(
                        selected = !state.isBinaryData,
                        onClick = { viewModel.toggleBinaryData(false) },
                        label = { Text("Decimal (1000ⁿ)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                    Spacer(Modifier.width(12.dp))
                    FilterChip(
                        selected = state.isBinaryData,
                        onClick = { viewModel.toggleBinaryData(true) },
                        label = { Text("Binary (1024ⁿ)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            // FROM Card
            UnitCard(
                label = "FROM",
                value = state.fromValue,
                unitSymbol = if (state.category == ConverterCategory.Temperature) state.fromTemp.symbol else state.fromUnit.symbol,
                unitName = if (state.category == ConverterCategory.Temperature) state.fromTemp.name else state.fromUnit.nameResKey,
                isActive = state.activeCard == ActiveInputCard.FROM,
                onClick = { viewModel.setActiveCard(ActiveInputCard.FROM) },
                onUnitClick = { showFromPicker = true },
            )

            // Circular Swap Button (44dp, gradient, 180° rotation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rotate(animatedRotation)
                        .clip(CircleShape)
                        .background(brandGradientBrush())
                        .clickable {
                            rotationAngle += 180f
                            viewModel.swapUnits()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = "Swap units",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // TO Card
            UnitCard(
                label = "TO",
                value = state.toValue,
                unitSymbol = if (state.category == ConverterCategory.Temperature) state.toTemp.symbol else state.toUnit.symbol,
                unitName = if (state.category == ConverterCategory.Temperature) state.toTemp.name else state.toUnit.nameResKey,
                isActive = state.activeCard == ActiveInputCard.TO,
                onClick = { viewModel.setActiveCard(ActiveInputCard.TO) },
                onUnitClick = { showToPicker = true },
            )

            Spacer(Modifier.height(16.dp))

            // Expandable "All units" list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleAllUnitsExpanded() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("All units", color = colors.textDim, fontSize = 14.sp)
                Icon(
                    imageVector = if (state.isAllUnitsExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textDim,
                )
            }

            AnimatedVisibility(visible = state.isAllUnitsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    state.allUnitsValues.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Unit Value", row.value))
                                    scope.launch { snackbarHostState.showSnackbar("Copied ${row.value}") }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(row.unit.nameResKey, color = colors.textDim, fontSize = 14.sp)
                            Text("${row.value} ${row.unit.symbol}", color = colors.textMain, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Numeric Keypad
            NumericKeypad(
                onDigit = { viewModel.onDigit(it) },
                onDecimal = { viewModel.onDecimal() },
                onSign = { viewModel.onSign() },
                onDelete = { viewModel.onDelete() },
                onClear = { viewModel.onClear() },
                showSign = state.category == ConverterCategory.Temperature,
            )
        }

        // Unit picker bottom sheets
        if (showFromPicker) {
            if (state.category == ConverterCategory.Temperature) {
                TemperaturePickerSheet(
                    selected = state.fromTemp,
                    onSelect = { viewModel.setFromTemp(it) },
                    onDismiss = { showFromPicker = false },
                )
            } else {
                UnitPickerSheet(
                    units = state.availableUnits,
                    selectedUnit = state.fromUnit,
                    onSelectUnit = { viewModel.setFromUnit(it) },
                    onDismiss = { showFromPicker = false },
                )
            }
        }

        if (showToPicker) {
            if (state.category == ConverterCategory.Temperature) {
                TemperaturePickerSheet(
                    selected = state.toTemp,
                    onSelect = { viewModel.setToTemp(it) },
                    onDismiss = { showToPicker = false },
                )
            } else {
                UnitPickerSheet(
                    units = state.availableUnits,
                    selectedUnit = state.toUnit,
                    onSelectUnit = { viewModel.setToUnit(it) },
                    onDismiss = { showToPicker = false },
                )
            }
        }
    }
}

@Composable
private fun UnitCard(
    label: String,
    value: String,
    unitSymbol: String,
    unitName: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onUnitClick: () -> Unit,
) {
    val colors = mathlatorColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = if (isActive) BorderStroke(1.5.dp, brush = brandGradientBrush()) else BorderStroke(1.dp, colors.divider),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = colors.textDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifEmpty { "0" },
                    color = colors.textMain,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )

                // Unit Chip
                Surface(
                    onClick = onUnitClick,
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceHigh,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$unitName ($unitSymbol)",
                            color = colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.textDim,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemperaturePickerSheet(
    selected: TempUnit,
    onSelect: (TempUnit) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = mathlatorColors
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Select temperature unit", color = colors.textMain, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            TempUnit.entries.forEach { temp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(temp)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(temp.name, color = if (selected == temp) Primary else colors.textMain, fontSize = 16.sp)
                    Text(temp.symbol, color = colors.textDim, fontSize = 14.sp)
                }
                HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
            }
        }
    }
}
