package com.mathlator.app.feature.finance.currency

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.TacticalConverterKeypad
import com.mathlator.app.ui.components.TacticalIcons
import com.mathlator.app.ui.components.tacticalCard
import com.mathlator.app.ui.components.tacticalExtrudedKeycap
import com.mathlator.app.ui.components.tacticalRecessedWell
import com.mathlator.app.ui.components.tacticalSocket
import com.mathlator.app.ui.theme.MathlatorTheme
import com.mathlator.app.ui.theme.TacticalColors
import com.mathlator.app.ui.theme.TacticalDimens
import com.mathlator.app.ui.theme.TacticalTypography
import com.mathlator.app.ui.theme.TitleTextStyle
import kotlinx.coroutines.launch

fun flagForCurrency(code: String): String {
    val country = when (code) {
        "EUR" -> "EU"
        "USD" -> "US"
        "GBP" -> "GB"
        "INR" -> "IN"
        "AED" -> "AE"
        "JPY" -> "JP"
        "AUD" -> "AU"
        "CAD" -> "CA"
        "CHF" -> "CH"
        "CNY" -> "CN"
        "SGD" -> "SG"
        else -> if (code.length >= 2) code.take(2) else ""
    }
    return if (country.length == 2) {
        val f = Character.codePointAt(country, 0) - 0x41 + 0x1F1E6
        val s = Character.codePointAt(country, 1) - 0x41 + 0x1F1E6
        String(Character.toChars(f)) + String(Character.toChars(s))
    } else "🌐"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var activePicker by remember { mutableStateOf<PickerTarget?>(null) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun copyAmount(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Currency Amount", value))
        scope.launch { snackbarHostState.showSnackbar("Copied $value") }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalColors.ObsidianChassis,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Multi-Currency Rack", style = TitleTextStyle, color = TacticalColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TacticalColors.TextPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadRates(force = true) }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TacticalColors.TacticalOrange,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = "Refresh rates",
                                tint = TacticalColors.TextPrimary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalColors.ObsidianChassis),
            )
        },
    ) { innerPadding ->
        if (state.isNoData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No exchange rates available", color = TacticalColors.TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadRates(force = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalColors.TacticalOrange),
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            ) {
                // ── Live Sync Header ─────────────────────────────────────────
                LiveSyncHeader(
                    timestampText = state.lastUpdatedText,
                    attributionText = state.attributionText,
                    isOffline = state.isOffline,
                    isLoading = state.isLoading,
                    onRefresh = { viewModel.loadRates(force = true) },
                )

                Spacer(Modifier.height(10.dp))

                // ── 3-Slot Currency Rack ─────────────────────────────────────
                // Slot 1: MAIN BASE (Highlighted with #FF5E00 border glow)
                MainRackSlot(
                    amount = state.mainValue,
                    currencyCode = state.mainCode,
                    onCurrencyClick = { activePicker = PickerTarget.MAIN },
                )

                Spacer(Modifier.height(10.dp))

                // Slot 2: CONVERTED TARGET 1
                TargetRackSlot(
                    targetLabel = "TARGET 1",
                    amount = state.out1Value.ifEmpty { "—" },
                    currencyCode = state.out1Code,
                    rateCaption = state.out1Rate,
                    onCurrencyClick = { activePicker = PickerTarget.OUT1 },
                    onPromote = { viewModel.promoteToMain(PickerTarget.OUT1) },
                )

                Spacer(Modifier.height(10.dp))

                // Slot 3: CONVERTED TARGET 2
                TargetRackSlot(
                    targetLabel = "TARGET 2",
                    amount = state.out2Value.ifEmpty { "—" },
                    currencyCode = state.out2Code,
                    rateCaption = state.out2Rate,
                    onCurrencyClick = { activePicker = PickerTarget.OUT2 },
                    onPromote = { viewModel.promoteToMain(PickerTarget.OUT2) },
                )

                Spacer(Modifier.weight(1f))

                // ── Bottom Keypad: TacticalConverterKeypad ────────────────────
                TacticalConverterKeypad(
                    onDigit = { viewModel.onDigit(it) },
                    onDecimal = { viewModel.onDecimal() },
                    onDelete = { viewModel.onDelete() },
                    onClear = { viewModel.onClear() },
                    onSwap = { viewModel.promoteToMain(PickerTarget.OUT1) },
                    onCopy = { copyAmount(state.out1Value.ifEmpty { state.mainValue }) },
                    showDecimal = true,
                )

                Spacer(Modifier.height(12.dp))
            }
        }

        // ── Currency Picker Sheet ─────────────────────────────────────────────
        activePicker?.let { target ->
            val selectedCode = when (target) {
                PickerTarget.MAIN -> state.mainCode
                PickerTarget.OUT1 -> state.out1Code
                PickerTarget.OUT2 -> state.out2Code
            }
            CurrencyPickerSheet(
                currencies = viewModel.currencyList,
                selectedCode = selectedCode,
                onSelect = {
                    viewModel.setCurrency(target, it.code)
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
        }
    }
}

@Composable
private fun LiveSyncHeader(
    timestampText: String,
    attributionText: String,
    isOffline: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseAlpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            // Pulsing emerald status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOffline) TacticalColors.TacticalAmber
                        else TacticalColors.EmeraldTelemetry.copy(alpha = pulseAlpha)
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$timestampText • $attributionText",
                color = if (isOffline) TacticalColors.TacticalAmber else TacticalColors.TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .tacticalSocket(cornerRadius = 8.dp)
                .clickable(onClick = onRefresh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Sync",
                tint = TacticalColors.TacticalOrange,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun MainRackSlot(
    amount: String,
    currencyCode: String,
    onCurrencyClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .tacticalRecessedWell(
                cornerRadius = TacticalDimens.WellCornerRadius,
                isFocused = true,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MAIN BASE // LIVE INPUT",
                style = TacticalTypography.TelemetryMicroBadge,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = amount.ifEmpty { "0" },
                    style = TacticalTypography.OledReadoutMedium.copy(
                        fontSize = 32.sp,
                        color = TacticalColors.TextPrimary,
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )

                CurrencySelectorPill(code = currencyCode, onClick = onCurrencyClick)
            }
        }
    }
}

@Composable
private fun TargetRackSlot(
    targetLabel: String,
    amount: String,
    currencyCode: String,
    rateCaption: String,
    onCurrencyClick: () -> Unit,
    onPromote: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .tacticalCard(cornerRadius = TacticalDimens.CardCornerRadius)
            .clickable(onClick = onPromote)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = targetLabel,
                style = TacticalTypography.TelemetryLabel.copy(fontSize = 10.sp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amount,
                        style = TacticalTypography.OledReadoutMedium.copy(
                            fontSize = 28.sp,
                            color = TacticalColors.TextPrimary,
                        ),
                        maxLines = 1,
                    )
                    if (rateCaption.isNotEmpty()) {
                        Text(
                            text = rateCaption,
                            color = TacticalColors.TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tactile Promote Button [ ⇅ ]
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .tacticalExtrudedKeycap(
                                cornerRadius = 10.dp,
                                isPressed = false,
                                isAccent = true,
                            )
                            .background(TacticalColors.KeycapEqualsBase)
                            .clickable(onClick = onPromote),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TacticalIcons.Swap,
                            contentDescription = "Promote to main",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    CurrencySelectorPill(code = currencyCode, onClick = onCurrencyClick)
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectorPill(code: String, onClick: () -> Unit) {
    val flag = flagForCurrency(code)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TacticalColors.KeycapUtilBase)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = flag, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$code ▾",
                color = TacticalColors.TacticalOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    currencies: List<CurrencyItem>,
    selectedCode: String,
    onSelect: (CurrencyItem) -> Unit,
    onDismiss: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filtered = remember(search, currencies) {
        if (search.isBlank()) currencies
        else currencies.filter {
            it.code.contains(search, ignoreCase = true) ||
                it.name.contains(search, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TacticalColors.SurfaceCard,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Select Currency",
                color = TacticalColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search currency…", color = TacticalColors.TextMuted) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TacticalColors.TacticalOrange)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TacticalColors.TacticalOrange,
                    cursorColor = TacticalColors.TacticalOrange,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = TacticalColors.SurfaceSocket,
                    unfocusedContainerColor = TacticalColors.SurfaceSocket,
                    focusedTextColor = TacticalColors.TextPrimary,
                    unfocusedTextColor = TacticalColors.TextPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                items(filtered, key = { it.code }) { item ->
                    val isSelected = item.code == selectedCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.flagEmoji, fontSize = 22.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    item.code,
                                    color = if (isSelected) TacticalColors.TacticalOrange else TacticalColors.TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                                Text(item.name, color = TacticalColors.TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0x14FFFFFF), thickness = 0.5.dp)
                }
            }
        }
    }
}
