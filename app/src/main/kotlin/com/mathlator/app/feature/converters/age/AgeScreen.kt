package com.mathlator.app.feature.converters.age

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.tacticalCard
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import com.mathlator.app.ui.theme.TitleTextStyle
import kotlinx.coroutines.launch
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
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDobPicker by remember { mutableStateOf(false) }
    var showAgeAtPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalColors.ObsidianChassis,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Age", style = TitleTextStyle, color = TacticalColors.TextPrimary) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ── Dual Recessed Date Picker Cards ───────────────────────────────
            RecessedDatePickerCard(
                badge = "Date of birth",
                dateStr = state.dob.format(dateFormatter),
                onClick = { showDobPicker = true },
            )

            Spacer(Modifier.height(10.dp))

            RecessedDatePickerCard(
                badge = "Age at target date",
                dateStr = state.ageAt.format(dateFormatter),
                onClick = { showAgeAtPicker = true },
            )

            Spacer(Modifier.height(18.dp))

            if (!state.isValid) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tacticalRecessedWell(cornerRadius = 16.dp)
                        .padding(16.dp),
                ) {
                    Text(
                        text = state.errorMessage,
                        color = Color(0xFFFF4D4D),
                        fontSize = 14.sp,
                    )
                }
            } else {
                val r = state.result
                val headline = "${r.years} years, ${r.months} months, ${r.days} days"

                // ── Hero Telemetry Card ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tacticalRecessedWell(
                            cornerRadius = TacticalDimens.WellCornerRadius,
                            isFocused = true,
                        )
                        .padding(18.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Glowing orange left accent bar
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TacticalColors.KeycapEqualsBase)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CHRONO TELEMETRY READOUT",
                                style = TacticalTypography.TelemetryMicroBadge,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = headline,
                                style = TacticalTypography.OledReadoutMedium.copy(
                                    fontSize = 22.sp,
                                    color = TacticalColors.TacticalOrange,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── 2×2 Bento Breakdown Sub-Grid ─────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AgeBentoTile(
                            label = "Total months",
                            value = "${r.totalMonths}",
                            unit = "months",
                            modifier = Modifier.weight(1f),
                        )
                        AgeBentoTile(
                            label = "Total weeks",
                            value = "${r.totalWeeks}",
                            unit = "weeks",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AgeBentoTile(
                            label = "Total days",
                            value = "${r.totalDays}",
                            unit = "days",
                            modifier = Modifier.weight(1f),
                        )
                        AgeBentoTile(
                            label = "Total hours",
                            value = "${r.totalHours}",
                            unit = "hours",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Next Birthday Countdown Bar ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tacticalCard(cornerRadius = 18.dp)
                        .padding(16.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Next birthday",
                                    style = TacticalTypography.TelemetryLabel,
                                )
                                Text(
                                    text = "${r.nextBirthdayDate.format(dateFormatter)} (${r.nextBirthdayWeekday})",
                                    color = TacticalColors.TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            // Badge: in X days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TacticalColors.KeycapUtilBase)
                                    .border(1.dp, TacticalColors.TacticalOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "in ${r.nextBirthdayDaysRemaining} days",
                                    color = TacticalColors.TacticalOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Linear countdown progress bar
                        val progress = ((365 - r.nextBirthdayDaysRemaining).coerceAtLeast(0) / 365f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TacticalColors.TacticalOrange,
                            trackColor = TacticalColors.SurfaceSocket,
                        )

                        Spacer(Modifier.height(12.dp))

                        // Action Buttons: Copy / Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            IconButton(onClick = {
                                val summary = "Age: $headline\nNext Birthday: in ${r.nextBirthdayDaysRemaining} days"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Age Result", summary))
                                scope.launch { snackbarHostState.showSnackbar("Age summary copied") }
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy age",
                                    tint = TacticalColors.TacticalOrange,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(onClick = {
                                val summary = "Age: $headline\nNext Birthday: in ${r.nextBirthdayDaysRemaining} days"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Age Telemetry"))
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share age",
                                    tint = TacticalColors.TacticalOrange,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
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
private fun RecessedDatePickerCard(
    badge: String,
    dateStr: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tacticalRecessedWell(cornerRadius = 16.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = badge,
                    style = TacticalTypography.TelemetryLabel.copy(fontSize = 10.sp),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    color = TacticalColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .tacticalSocket(cornerRadius = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Pick date",
                    tint = TacticalColors.TacticalOrange,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AgeBentoTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .tacticalCard(cornerRadius = 16.dp)
            .padding(14.dp),
    ) {
        Column {
            Text(label, style = TacticalTypography.TelemetryLabel.copy(fontSize = 10.sp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = TacticalTypography.TelemetryValue.copy(
                    fontSize = 18.sp,
                    color = TacticalColors.TextPrimary,
                ),
            )
            Text(
                text = unit,
                style = TacticalTypography.BentoSubtitle.copy(color = TacticalColors.TextMuted),
            )
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
                Text("OK", color = TacticalColors.TacticalOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TacticalColors.TextSecondary)
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
