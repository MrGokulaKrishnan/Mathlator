package com.mathlator.app.feature.converters.date

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.ResultCard
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateScreen(
    viewModel: DateViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showBasePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Date", style = TitleTextStyle, color = colors.textMain) },
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
                .verticalScroll(rememberScrollState()),
        ) {
            // Two tabs: Difference | Add / Subtract
            TabRow(
                selectedTabIndex = if (state.selectedTab == DateTab.DIFFERENCE) 0 else 1,
                containerColor = colors.background,
                contentColor = Primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (state.selectedTab == DateTab.DIFFERENCE) 0 else 1]),
                        color = Primary,
                    )
                },
            ) {
                Tab(
                    selected = state.selectedTab == DateTab.DIFFERENCE,
                    onClick = { viewModel.selectTab(DateTab.DIFFERENCE) },
                    text = { Text("Difference", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                    selectedContentColor = Primary,
                    unselectedContentColor = colors.textDim,
                )
                Tab(
                    selected = state.selectedTab == DateTab.ADD_SUBTRACT,
                    onClick = { viewModel.selectTab(DateTab.ADD_SUBTRACT) },
                    text = { Text("Add / Subtract", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                    selectedContentColor = Primary,
                    unselectedContentColor = colors.textDim,
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (state.selectedTab == DateTab.DIFFERENCE) {
                    // Difference Tab Content
                    DatePickerRow(
                        label = "Start date",
                        dateStr = state.startDate.format(dateFormatter),
                        onClick = { showStartPicker = true },
                    )
                    Spacer(Modifier.height(12.dp))
                    DatePickerRow(
                        label = "End date",
                        dateStr = state.endDate.format(dateFormatter),
                        onClick = { showEndPicker = true },
                    )

                    Spacer(Modifier.height(20.dp))

                    val diff = state.differenceResult
                    val headline = "${diff.years} years, ${diff.months} months, ${diff.days} days"
                    val rows = listOf(
                        "Total days" to "${diff.totalDays} days",
                        "Weeks + days" to "${diff.weeks} weeks, ${diff.remainingDays} days",
                    )

                    ResultCard(
                        headline = headline,
                        rows = rows,
                        onCopy = {},
                        onShare = {},
                    )
                } else {
                    // Add / Subtract Tab Content
                    DatePickerRow(
                        label = "Base date",
                        dateStr = state.baseDate.format(dateFormatter),
                        onClick = { showBasePicker = true },
                    )

                    Spacer(Modifier.height(16.dp))

                    // + / - Toggle
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = state.isAdd,
                            onClick = { viewModel.setIsAdd(true) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Primary,
                                activeContentColor = Color.White,
                                inactiveContainerColor = colors.surfaceHigh,
                                inactiveContentColor = colors.textMain,
                            ),
                        ) {
                            Text("Add (+)")
                        }
                        SegmentedButton(
                            selected = !state.isAdd,
                            onClick = { viewModel.setIsAdd(false) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Primary,
                                activeContentColor = Color.White,
                                inactiveContainerColor = colors.surfaceHigh,
                                inactiveContentColor = colors.textMain,
                            ),
                        ) {
                            Text("Subtract (−)")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Steppers for Years, Months, Days
                    StepperRow("Years", state.yearsDelta) { viewModel.setYearsDelta(it) }
                    Spacer(Modifier.height(8.dp))
                    StepperRow("Months", state.monthsDelta) { viewModel.setMonthsDelta(it) }
                    Spacer(Modifier.height(8.dp))
                    StepperRow("Days", state.daysDelta) { viewModel.setDaysDelta(it) }

                    Spacer(Modifier.height(20.dp))

                    ResultCard(
                        headline = state.resultingDate.format(dateFormatter),
                        rows = listOf("Weekday" to state.resultingWeekday),
                        onCopy = {},
                        onShare = {},
                    )
                }
            }
        }

        // Dialogs for Date Picker
        if (showStartPicker) {
            DatePickerModal(
                initialDate = state.startDate,
                onDateSelected = {
                    viewModel.setStartDate(it)
                    showStartPicker = false
                },
                onDismiss = { showStartPicker = false },
            )
        }
        if (showEndPicker) {
            DatePickerModal(
                initialDate = state.endDate,
                onDateSelected = {
                    viewModel.setEndDate(it)
                    showEndPicker = false
                },
                onDismiss = { showEndPicker = false },
            )
        }
        if (showBasePicker) {
            DatePickerModal(
                initialDate = state.baseDate,
                onDateSelected = {
                    viewModel.setBaseDate(it)
                    showBasePicker = false
                },
                onDismiss = { showBasePicker = false },
            )
        }
    }
}

@Composable
private fun DatePickerRow(
    label: String,
    dateStr: String,
    onClick: () -> Unit,
) {
    val colors = mathlatorColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceHigh, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(label, color = colors.textDim, fontSize = 12.sp)
            Text(dateStr, color = colors.textMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pick date", tint = Primary)
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val colors = mathlatorColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceHigh, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.textMain, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0)) }) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = colors.operator)
            }
            Text(
                text = "$value",
                color = colors.textMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            IconButton(onClick = { onValueChange(value + 1) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = Primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val epochMilli = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMilli)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                }
            ) {
                Text("OK", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = mathlatorColors.textDim)
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
