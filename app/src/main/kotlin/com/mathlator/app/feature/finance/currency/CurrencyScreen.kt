package com.mathlator.app.feature.finance.currency

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.components.NumericKeypad
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.TitleTextStyle
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val state by viewModel.uiState.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 300),
        label = "CurrencySwapRotation",
    )

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
                    IconButton(onClick = { viewModel.loadRates(force = true) }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh rates", tint = colors.textMain)
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
                        onClick = { viewModel.loadRates(force = true) },
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                // Rates status line
                Text(
                    text = state.lastUpdatedText,
                    color = if (state.isOffline) colors.operator else colors.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                // FROM Card
                CurrencyCard(
                    label = "FROM",
                    amount = state.fromValue,
                    currencyCode = state.fromCode,
                    onCurrencyClick = { showFromPicker = true },
                )

                // Swap Button
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
                                viewModel.swapCurrencies()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = "Swap currencies",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                // TO Card
                CurrencyCard(
                    label = "TO",
                    amount = state.toValue,
                    currencyCode = state.toCode,
                    onCurrencyClick = { showToPicker = true },
                )

                Spacer(Modifier.height(16.dp))

                // Numeric Keypad
                NumericKeypad(
                    onDigit = { viewModel.onDigit(it) },
                    onDecimal = { viewModel.onDecimal() },
                    onSign = {},
                    onDelete = { viewModel.onDelete() },
                    onClear = { viewModel.onClear() },
                    showSign = false,
                )

                Spacer(Modifier.height(16.dp))

                // Attribution footer
                Text(
                    text = state.attributionText,
                    color = colors.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        // Currency Picker sheets
        if (showFromPicker) {
            CurrencyPickerSheet(
                currencies = viewModel.currencyList,
                selectedCode = state.fromCode,
                onSelect = {
                    viewModel.setFromCurrency(it.code)
                    showFromPicker = false
                },
                onDismiss = { showFromPicker = false },
            )
        }

        if (showToPicker) {
            CurrencyPickerSheet(
                currencies = viewModel.currencyList,
                selectedCode = state.toCode,
                onSelect = {
                    viewModel.setToCurrency(it.code)
                    showToPicker = false
                },
                onDismiss = { showToPicker = false },
            )
        }
    }
}

@Composable
private fun CurrencyCard(
    label: String,
    amount: String,
    currencyCode: String,
    onCurrencyClick: () -> Unit,
) {
    val colors = mathlatorColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.divider),
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
                    text = amount.ifEmpty { "0" },
                    color = colors.textMain,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    onClick = onCurrencyClick,
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceHigh,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currencyCode,
                            color = colors.textMain,
                            fontSize = 16.sp,
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
            it.code.contains(search, ignoreCase = true) || it.name.contains(search, ignoreCase = true)
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
            Text("Select currency", color = colors.textMain, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search currency…", color = colors.textDim) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textDim) },
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
