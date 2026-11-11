package com.mathlator.app.feature.converters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mathlator.app.ui.components.TacticalConverterKeypad
import com.mathlator.app.ui.components.TacticalIcons
import com.mathlator.app.ui.components.tacticalExtrudedKeycap
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import com.mathlator.app.ui.theme.TitleTextStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

    fun copyToClipboard(valueToCopy: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Converted Value", valueToCopy))
        scope.launch { snackbarHostState.showSnackbar("Copied $valueToCopy") }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalColors.ObsidianChassis,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.category.name,
                        style = TitleTextStyle,
                        color = TacticalColors.TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TacticalColors.TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalColors.ObsidianChassis),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            // Data converter: Neumorphic Segmented Toggle
            if (state.category == ConverterCategory.Data) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalColors.SurfaceDock)
                            .padding(3.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SegmentedDataToggleItem(
                                label = "Decimal (1000ⁿ)",
                                isSelected = !state.isBinaryData,
                                onClick = { viewModel.toggleBinaryData(false) },
                            )
                            SegmentedDataToggleItem(
                                label = "Binary (1024ⁿ)",
                                isSelected = state.isBinaryData,
                                onClick = { viewModel.toggleBinaryData(true) },
                            )
                        }
                    }
                }
            }

            // Dual I/O Wells with Floating 3D Swap Orb overlapping the gap
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // FROM Card
                    TacticalUnitCard(
                        label = "FROM",
                        value = state.fromValue,
                        unitSymbol = if (state.category == ConverterCategory.Temperature) state.fromTemp.symbol else state.fromUnit.symbol,
                        unitName = if (state.category == ConverterCategory.Temperature) state.fromTemp.name else state.fromUnit.nameResKey,
                        isFocused = state.activeCard == ActiveInputCard.FROM,
                        onClick = { viewModel.setActiveCard(ActiveInputCard.FROM) },
                        onUnitClick = { showFromPicker = true },
                    )

                    Spacer(Modifier.height(14.dp))

                    // TO Card
                    TacticalUnitCard(
                        label = "TO",
                        value = state.toValue,
                        unitSymbol = if (state.category == ConverterCategory.Temperature) state.toTemp.symbol else state.toUnit.symbol,
                        unitName = if (state.category == ConverterCategory.Temperature) state.toTemp.name else state.toUnit.nameResKey,
                        isFocused = state.activeCard == ActiveInputCard.TO,
                        onClick = { viewModel.setActiveCard(ActiveInputCard.TO) },
                        onUnitClick = { showToPicker = true },
                    )
                }

                // Floating 3D Center Swap Orb (zIndex = 2f, positioned in gap)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(2f),
                    contentAlignment = Alignment.Center,
                ) {
                    val swapSource = remember { MutableInteractionSource() }
                    val swapPressed by swapSource.collectIsPressedAsState()

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rotate(animatedRotation)
                            .tacticalExtrudedKeycap(
                                cornerRadius = 22.dp,
                                isPressed = swapPressed,
                                isAccent = true,
                            )
                            .background(TacticalColors.KeycapEqualsBase)
                            .clickable(
                                interactionSource = swapSource,
                                indication = null,
                                onClick = {
                                    rotationAngle += 180f
                                    viewModel.swapUnits()
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TacticalIcons.Swap,
                            contentDescription = "Swap units",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Expandable "All units" Live Drawer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TacticalColors.SurfaceSocket)
                    .clickable { viewModel.toggleAllUnitsExpanded() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "All units preview (${state.allUnitsValues.size})",
                        style = TacticalTypography.TelemetryLabel.copy(color = TacticalColors.TacticalOrange),
                    )
                    Icon(
                        imageVector = if (state.isAllUnitsExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TacticalColors.TacticalOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            AnimatedVisibility(visible = state.isAllUnitsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 12.dp)
                ) {
                    state.allUnitsValues.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { copyToClipboard(row.value) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(row.unit.nameResKey, color = TacticalColors.TextSecondary, fontSize = 13.sp)
                            Text(
                                text = "${row.value} ${row.unit.symbol}",
                                color = TacticalColors.TextPrimary,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        HorizontalDivider(color = Color(0x14FFFFFF), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Uniform 4×4 Tactical Converter Keypad ────────────────────────
            TacticalConverterKeypad(
                onDigit = { viewModel.onDigit(it) },
                onDecimal = { viewModel.onDecimal() },
                onDelete = { viewModel.onDelete() },
                onClear = { viewModel.onClear() },
                onSwap = {
                    rotationAngle += 180f
                    viewModel.swapUnits()
                },
                onCopy = {
                    val activeVal = if (state.activeCard == ActiveInputCard.FROM) state.toValue else state.fromValue
                    copyToClipboard(activeVal.ifEmpty { "0" })
                },
                onSign = { viewModel.onSign() },
                showSign = state.category == ConverterCategory.Temperature,
            )

            Spacer(Modifier.height(16.dp))
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
private fun TacticalUnitCard(
    label: String,
    value: String,
    unitSymbol: String,
    unitName: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    onUnitClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tacticalRecessedWell(
                cornerRadius = TacticalDimens.WellCornerRadius,
                isFocused = isFocused,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = TacticalTypography.TelemetryMicroBadge.copy(
                    color = if (isFocused) TacticalColors.TacticalOrange else TacticalColors.TextMuted,
                ),
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifEmpty { "0" },
                    style = TacticalTypography.OledReadoutMedium.copy(
                        fontSize = 32.sp,
                        color = if (isFocused) TacticalColors.TextPrimary else TacticalColors.TextSecondary,
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )

                // Tactical Unit Selector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TacticalColors.KeycapUtilBase)
                        .clickable(onClick = onUnitClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$unitSymbol ▾",
                            color = TacticalColors.TacticalOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedDataToggleItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isSelected) Modifier.background(TacticalColors.KeycapEqualsBase)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TacticalColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemperaturePickerSheet(
    selected: TempUnit,
    onSelect: (TempUnit) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TacticalColors.SurfaceCard,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Select temperature unit", color = TacticalColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
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
                    Text(
                        text = temp.name,
                        color = if (selected == temp) TacticalColors.TacticalOrange else TacticalColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = if (selected == temp) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(temp.symbol, color = TacticalColors.TextSecondary, fontSize = 14.sp)
                }
                HorizontalDivider(color = Color(0x14FFFFFF), thickness = 0.5.dp)
            }
        }
    }
}
