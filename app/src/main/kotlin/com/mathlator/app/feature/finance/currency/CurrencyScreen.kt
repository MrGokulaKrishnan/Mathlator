package com.mathlator.app.feature.finance.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.NumericKeypad
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.BrandEnd
import com.mathlator.app.ui.theme.BrandStart
import com.mathlator.app.ui.theme.MathlatorTheme
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    // Which picker is showing
    var activePicker by remember { mutableStateOf<PickerTarget?>(null) }

    // Adapt card height for shorter screens (< 700dp)
    val isCompact = screenHeightDp < 700
    val cardHeight = if (isCompact) 80.dp else 96.dp
    val amountFontSize = if (isCompact) 32.sp else 36.sp

    CurrencyScreenContent(
        state = state,
        isCompact = isCompact,
        cardHeight = cardHeight,
        amountFontSize = amountFontSize,
        onNavigateBack = onNavigateBack,
        onRefresh = { viewModel.loadRates(force = true) },
        onPromote = { viewModel.promoteToMain(it) },
        onCurrencyClick = { activePicker = it },
        onDigit = { viewModel.onDigit(it) },
        onDecimal = { viewModel.onDecimal() },
        onDelete = { viewModel.onDelete() },
        onClear = { viewModel.onClear() },
        modifier = modifier,
    )

    // ── Currency Picker Sheet ─────────────────────────────────────────────────
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreenContent(
    state: CurrencyUiState,
    isCompact: Boolean,
    cardHeight: Dp,
    amountFontSize: TextUnit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onPromote: (PickerTarget) -> Unit,
    onCurrencyClick: (PickerTarget) -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Currency", style = TitleTextStyle, color = colors.textMain) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textMain,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Primary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = "Refresh rates",
                                tint = colors.textMain,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
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
                    Text("No exchange rates available", color = colors.textMain, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
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
                    .padding(horizontal = 20.dp),
            ) {
                // Rates status line
                Text(
                    text = state.lastUpdatedText,
                    color = if (state.isOffline) colors.operator else colors.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp),
                )

                // ── Card 1: MAIN ─────────────────────────────────────────────
                MainCurrencyCard(
                    amount = state.mainValue,
                    currencyCode = state.mainCode,
                    cardHeight = cardHeight.value.toInt(),
                    amountFontSize = amountFontSize.value.toInt(),
                    onCurrencyClick = { onCurrencyClick(PickerTarget.MAIN) },
                )

                Spacer(Modifier.height(10.dp))

                // ── Card 2: OUTPUT 1 ─────────────────────────────────────────
                ConvertedCurrencyCard(
                    label = "CONVERTED",
                    amount = state.out1Value.ifEmpty { "—" },
                    currencyCode = state.out1Code,
                    rateCaption = state.out1Rate,
                    cardHeight = cardHeight.value.toInt(),
                    amountFontSize = amountFontSize.value.toInt(),
                    onCurrencyClick = { onCurrencyClick(PickerTarget.OUT1) },
                    onPromote = { onPromote(PickerTarget.OUT1) },
                )

                Spacer(Modifier.height(10.dp))

                // ── Card 3: OUTPUT 2 ─────────────────────────────────────────
                ConvertedCurrencyCard(
                    label = "CONVERTED",
                    amount = state.out2Value.ifEmpty { "—" },
                    currencyCode = state.out2Code,
                    rateCaption = state.out2Rate,
                    cardHeight = cardHeight.value.toInt(),
                    amountFontSize = amountFontSize.value.toInt(),
                    onCurrencyClick = { onCurrencyClick(PickerTarget.OUT2) },
                    onPromote = { onPromote(PickerTarget.OUT2) },
                )

                // Push keypad to bottom
                Spacer(Modifier.weight(1f))

                // ── Keypad (Bug 3 layout with tall ⌫ and C, no empty cells) ─
                NumericKeypad(
                    onDigit = onDigit,
                    onDecimal = onDecimal,
                    onSign = {},
                    onDelete = onDelete,
                    onClear = onClear,
                    showDecimal = true,
                    showSign = false,
                )

                Spacer(Modifier.height(6.dp))

                // Attribution footer
                Text(
                    text = state.attributionText,
                    color = colors.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MainCurrencyCard(
    amount: String,
    currencyCode: String,
    cardHeight: Int,
    amountFontSize: Int,
    onCurrencyClick: () -> Unit,
) {
    val colors = mathlatorColors
    val gradientBorder = Brush.linearGradient(listOf(BrandStart, BrandEnd))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, gradientBorder, RoundedCornerShape(20.dp))
            .background(colors.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                "MAIN",
                color = colors.textDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.08.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = amount.ifEmpty { "0" },
                    color = colors.textMain,
                    fontSize = amountFontSize.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                CurrencyChip(code = currencyCode, onClick = onCurrencyClick)
            }
        }
    }
}

@Composable
private fun ConvertedCurrencyCard(
    label: String,
    amount: String,
    currencyCode: String,
    rateCaption: String,
    cardHeight: Int,
    amountFontSize: Int,
    onCurrencyClick: () -> Unit,
    onPromote: () -> Unit,
) {
    val colors = mathlatorColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .clickable { onPromote() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                label,
                color = colors.textDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.08.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amount,
                        color = colors.textMain,
                        fontSize = amountFontSize.sp,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                    )
                    if (rateCaption.isNotEmpty()) {
                        Text(
                            text = rateCaption,
                            color = colors.textDim,
                            fontSize = 12.sp,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Promote-to-main swap button (40dp)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(brandGradientBrush())
                            .clickable { onPromote() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = "Make main",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    CurrencyChip(code = currencyCode, onClick = onCurrencyClick)
                }
            }
        }
    }
}

@Composable
private fun CurrencyChip(code: String, onClick: () -> Unit) {
    val colors = mathlatorColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = code,
                color = colors.textMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "Change currency",
                tint = colors.textDim,
                modifier = Modifier.size(18.dp),
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
    val colors = mathlatorColors
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
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                "Select currency",
                color = colors.textMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search currency…", color = colors.textDim) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textDim)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    cursorColor = Primary,
                    unfocusedBorderColor = colors.divider,
                    focusedContainerColor = colors.surfaceHigh,
                    unfocusedContainerColor = colors.surfaceHigh,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                items(filtered, key = { it.code }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.flagEmoji, fontSize = 24.sp)
                            Spacer(Modifier.size(12.dp))
                            Column {
                                Text(
                                    item.code,
                                    color = if (item.code == selectedCode) Primary else colors.textMain,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(item.name, color = colors.textDim, fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Currency Pixel Portrait Dark", device = "id:pixel_7", showSystemUi = true)
@Composable
private fun CurrencyPreviewPixel() {
    MathlatorTheme(isDark = true) {
        CurrencyScreenContent(
            state = CurrencyUiState(
                mainCode = "INR",
                out1Code = "USD",
                out2Code = "EUR",
                mainValue = "900",
                out1Value = "10",
                out2Value = "9",
                out1Rate = "1 INR = 0.0111 USD",
                out2Rate = "1 INR = 0.0100 EUR",
                lastUpdatedText = "Rates updated just now",
            ),
            isCompact = false,
            cardHeight = 96.dp,
            amountFontSize = 36.sp,
            onNavigateBack = {},
            onRefresh = {},
            onPromote = {},
            onCurrencyClick = {},
            onDigit = {},
            onDecimal = {},
            onDelete = {},
            onClear = {},
        )
    }
}

@Preview(name = "Currency Small Phone 360x640", widthDp = 360, heightDp = 640)
@Composable
private fun CurrencyPreviewSmall() {
    MathlatorTheme(isDark = true) {
        CurrencyScreenContent(
            state = CurrencyUiState(
                mainCode = "INR",
                out1Code = "USD",
                out2Code = "EUR",
                mainValue = "100",
                out1Value = "1.11",
                out2Value = "1.00",
                lastUpdatedText = "Rates updated just now",
            ),
            isCompact = true,
            cardHeight = 80.dp,
            amountFontSize = 32.sp,
            onNavigateBack = {},
            onRefresh = {},
            onPromote = {},
            onCurrencyClick = {},
            onDigit = {},
            onDecimal = {},
            onDelete = {},
            onClear = {},
        )
    }
}

@Preview(name = "Currency Font Scale 1.3", fontScale = 1.3f)
@Composable
private fun CurrencyPreviewLargeFont() {
    MathlatorTheme(isDark = true) {
        CurrencyScreenContent(
            state = CurrencyUiState(
                mainCode = "USD",
                out1Code = "EUR",
                out2Code = "GBP",
                mainValue = "50",
                out1Value = "45",
                out2Value = "38.5",
                lastUpdatedText = "Rates updated 2h ago (offline)",
                isOffline = true,
            ),
            isCompact = false,
            cardHeight = 96.dp,
            amountFontSize = 36.sp,
            onNavigateBack = {},
            onRefresh = {},
            onPromote = {},
            onCurrencyClick = {},
            onDigit = {},
            onDecimal = {},
            onDelete = {},
            onClear = {},
        )
    }
}

@Preview(name = "Currency Light Theme")
@Composable
private fun CurrencyPreviewLight() {
    MathlatorTheme(isDark = false) {
        CurrencyScreenContent(
            state = CurrencyUiState(
                mainCode = "INR",
                out1Code = "USD",
                out2Code = "EUR",
                mainValue = "1000",
                out1Value = "11.11",
                out2Value = "10.00",
                lastUpdatedText = "Rates updated just now",
            ),
            isCompact = false,
            cardHeight = 96.dp,
            amountFontSize = 36.sp,
            onNavigateBack = {},
            onRefresh = {},
            onPromote = {},
            onCurrencyClick = {},
            onDigit = {},
            onDecimal = {},
            onDelete = {},
            onClear = {},
        )
    }
}
