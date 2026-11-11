package com.mathlator.app.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.core.engine.AngleMode
import com.mathlator.app.core.format.DigitGrouping
import com.mathlator.app.ui.components.MathlatorLogo
import com.mathlator.app.ui.theme.AppTheme
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val theme by viewModel.theme.collectAsState()
    val haptics by viewModel.haptics.collectAsState()
    val keySound by viewModel.keySound.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val precision by viewModel.decimalPrecision.collectAsState()
    val grouping by viewModel.digitGrouping.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val oneHandedSide by viewModel.oneHandedSide.collectAsState()
    val currencyFrom by viewModel.currencyFrom.collectAsState()
    val currencyTo by viewModel.currencyTo.collectAsState()
    val currencyOut2 by viewModel.currencyOut2.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = TitleTextStyle, color = colors.textMain) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textMain,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Mathlator Logo at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                MathlatorLogo(size = 28.dp)
            }

            Spacer(Modifier.height(16.dp))

            // SECTION: APPEARANCE
            SectionHeader("Appearance")

            Text("Theme", color = colors.textMain, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ThemeOption("Dark", AppTheme.Dark, theme) { viewModel.setTheme(it) }
                ThemeOption("Light", AppTheme.Light, theme) { viewModel.setTheme(it) }
                ThemeOption("System", AppTheme.System, theme) { viewModel.setTheme(it) }
            }

            SettingSwitch("Haptics", haptics) { viewModel.setHaptics(it) }
            SettingSwitch("Key sound", keySound) { viewModel.setKeySound(it) }

            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 16.dp))

            // SECTION: CALCULATOR
            SectionHeader("Calculator")

            Text("Angle mode", color = colors.textMain, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth()) {
                AngleOption("DEG", AngleMode.DEG, angleMode) { viewModel.setAngleMode(it) }
                Spacer(Modifier.padding(horizontal = 16.dp))
                AngleOption("RAD", AngleMode.RAD, angleMode) { viewModel.setAngleMode(it) }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Decimal precision", color = colors.textMain, fontSize = 16.sp)
                Text("$precision", color = Primary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Slider(
                value = precision.toFloat(),
                onValueChange = { viewModel.setDecimalPrecision(it.toInt()) },
                valueRange = 2f..15f,
                steps = 12,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = colors.surfaceHigh,
                ),
            )

            Spacer(Modifier.height(8.dp))
            Text("Digit grouping", color = colors.textMain, fontSize = 16.sp)
            GroupingOption("Indian (12,34,567)", DigitGrouping.Indian, grouping) { viewModel.setDigitGrouping(it) }
            GroupingOption("International (1,234,567)", DigitGrouping.International, grouping) { viewModel.setDigitGrouping(it) }
            GroupingOption("None", DigitGrouping.None, grouping) { viewModel.setDigitGrouping(it) }

            SettingSwitch("Keep screen on", keepScreenOn) { viewModel.setKeepScreenOn(it) }

            Spacer(Modifier.height(8.dp))
            Text("One-handed mode side", color = colors.textMain, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth()) {
                OneHandedOption("Left", "left", oneHandedSide) { viewModel.setOneHandedSide(it) }
                Spacer(Modifier.padding(horizontal = 16.dp))
                OneHandedOption("Right", "right", oneHandedSide) { viewModel.setOneHandedSide(it) }
            }

            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 16.dp))

            // SECTION: DATA
            SectionHeader("Data")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default currencies", color = colors.textMain, fontSize = 16.sp)
                    Text("$currencyFrom · $currencyTo · $currencyOut2", color = colors.textDim, fontSize = 12.sp)
                }
                TextButton(onClick = { showCurrencyDialog = true }) {
                    Text("Edit", color = Primary)
                }
            }

            if (showCurrencyDialog) {
                var editMain by remember(currencyFrom) { mutableStateOf(currencyFrom) }
                var editOut1 by remember(currencyTo) { mutableStateOf(currencyTo) }
                var editOut2 by remember(currencyOut2) { mutableStateOf(currencyOut2) }

                AlertDialog(
                    onDismissRequest = { showCurrencyDialog = false },
                    title = { Text("Default Currencies", color = colors.textMain) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editMain,
                                onValueChange = { editMain = it.uppercase().take(3) },
                                label = { Text("Main Currency", color = colors.textDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    focusedLabelColor = Primary,
                                    unfocusedBorderColor = colors.divider,
                                    focusedTextColor = colors.textMain,
                                    unfocusedTextColor = colors.textMain,
                                ),
                            )
                            OutlinedTextField(
                                value = editOut1,
                                onValueChange = { editOut1 = it.uppercase().take(3) },
                                label = { Text("Converted Currency 1", color = colors.textDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    focusedLabelColor = Primary,
                                    unfocusedBorderColor = colors.divider,
                                    focusedTextColor = colors.textMain,
                                    unfocusedTextColor = colors.textMain,
                                ),
                            )
                            OutlinedTextField(
                                value = editOut2,
                                onValueChange = { editOut2 = it.uppercase().take(3) },
                                label = { Text("Converted Currency 2", color = colors.textDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    focusedLabelColor = Primary,
                                    unfocusedBorderColor = colors.divider,
                                    focusedTextColor = colors.textMain,
                                    unfocusedTextColor = colors.textMain,
                                ),
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (editMain.isNotBlank()) viewModel.setCurrencyFrom(editMain)
                                if (editOut1.isNotBlank()) viewModel.setCurrencyTo(editOut1)
                                if (editOut2.isNotBlank()) viewModel.setCurrencyOut2(editOut2)
                                showCurrencyDialog = false
                            }
                        ) {
                            Text("Save", color = Primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCurrencyDialog = false }) {
                            Text("Cancel", color = colors.textDim)
                        }
                    },
                    containerColor = colors.surfaceHigh,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Clear history", color = colors.textMain, fontSize = 16.sp)
                    Text("Delete all saved calculations", color = colors.textDim, fontSize = 12.sp)
                }
                TextButton(
                    onClick = {
                        viewModel.clearHistory {
                            scope.launch { snackbarHostState.showSnackbar("History cleared") }
                        }
                    }
                ) {
                    Text("Clear", color = colors.error)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Clear cached rates", color = colors.textMain, fontSize = 16.sp)
                    Text("Force currency rates to re-fetch", color = colors.textDim, fontSize = 12.sp)
                }
                TextButton(
                    onClick = {
                        viewModel.clearCachedRates {
                            scope.launch { snackbarHostState.showSnackbar("Cached rates cleared") }
                        }
                    }
                ) {
                    Text("Clear", color = colors.operator)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = mathlatorColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = colors.textMain, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.background,
                checkedTrackColor = Primary,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedThumbColor = colors.textDim,
            ),
        )
    }
}

@Composable
private fun ThemeOption(label: String, target: AppTheme, current: AppTheme, onSelect: (AppTheme) -> Unit) {
    val colors = mathlatorColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect(target) }) {
        RadioButton(
            selected = current == target,
            onClick = { onSelect(target) },
            colors = RadioButtonDefaults.colors(selectedColor = Primary, unselectedColor = colors.textDim),
        )
        Text(label, color = colors.textMain, fontSize = 14.sp)
    }
}

@Composable
private fun AngleOption(label: String, target: AngleMode, current: AngleMode, onSelect: (AngleMode) -> Unit) {
    val colors = mathlatorColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect(target) }) {
        RadioButton(
            selected = current == target,
            onClick = { onSelect(target) },
            colors = RadioButtonDefaults.colors(selectedColor = Primary, unselectedColor = colors.textDim),
        )
        Text(label, color = colors.textMain, fontSize = 14.sp)
    }
}

@Composable
private fun GroupingOption(label: String, target: DigitGrouping, current: DigitGrouping, onSelect: (DigitGrouping) -> Unit) {
    val colors = mathlatorColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect(target) }) {
        RadioButton(
            selected = current == target,
            onClick = { onSelect(target) },
            colors = RadioButtonDefaults.colors(selectedColor = Primary, unselectedColor = colors.textDim),
        )
        Text(label, color = colors.textMain, fontSize = 14.sp)
    }
}

@Composable
private fun OneHandedOption(label: String, target: String, current: String, onSelect: (String) -> Unit) {
    val colors = mathlatorColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect(target) }) {
        RadioButton(
            selected = current == target,
            onClick = { onSelect(target) },
            colors = RadioButtonDefaults.colors(selectedColor = Primary, unselectedColor = colors.textDim),
        )
        Text(label, color = colors.textMain, fontSize = 14.sp)
    }
}
