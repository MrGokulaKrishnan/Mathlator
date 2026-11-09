package com.mathlator.app.feature.converters.age

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
fun AgeScreen(
    viewModel: AgeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    var showDobPicker by remember { mutableStateOf(false) }
    var showAgeAtPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Age", style = TitleTextStyle, color = colors.textMain) },
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // DOB Picker Row
            AgeDateRow(
                label = "Date of birth",
                dateStr = state.dob.format(dateFormatter),
                onClick = { showDobPicker = true },
            )

            Spacer(Modifier.height(12.dp))

            // Age At Picker Row
            AgeDateRow(
                label = "Age at",
                dateStr = state.ageAt.format(dateFormatter),
                onClick = { showAgeAtPicker = true },
            )

            Spacer(Modifier.height(20.dp))

            if (!state.isValid) {
                Text(
                    text = state.errorMessage,
                    color = colors.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                val r = state.result
                val headline = "${r.years} years, ${r.months} months, ${r.days} days"
                val rows = listOf(
                    "Total months" to "${r.totalMonths} months",
                    "Total weeks" to "${r.totalWeeks} weeks",
                    "Total days" to "${r.totalDays} days",
                    "Total hours" to "${r.totalHours} hours",
                    "Next birthday" to "${r.nextBirthdayDate.format(dateFormatter)} (${r.nextBirthdayWeekday})",
                    "Days until birthday" to "in ${r.nextBirthdayDaysRemaining} days",
                )

                ResultCard(
                    headline = headline,
                    rows = rows,
                    onCopy = {},
                    onShare = {},
                )
            }
        }

        if (showDobPicker) {
            AgeDatePickerModal(
                initialDate = state.dob,
                onDateSelected = {
                    viewModel.setDob(it)
                    showDobPicker = false
                },
                onDismiss = { showDobPicker = false },
            )
        }

        if (showAgeAtPicker) {
            AgeDatePickerModal(
                initialDate = state.ageAt,
                onDateSelected = {
                    viewModel.setAgeAt(it)
                    showAgeAtPicker = false
                },
                onDismiss = { showAgeAtPicker = false },
            )
        }
    }
}

@Composable
private fun AgeDateRow(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgeDatePickerModal(
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
