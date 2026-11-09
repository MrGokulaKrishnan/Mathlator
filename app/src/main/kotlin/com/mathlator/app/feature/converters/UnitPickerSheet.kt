package com.mathlator.app.feature.converters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.mathlatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPickerSheet(
    units: List<UnitDef>,
    selectedUnit: UnitDef,
    onSelectUnit: (UnitDef) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredUnits = remember(searchQuery, units) {
        if (searchQuery.isBlank()) units
        else units.filter {
            it.nameResKey.contains(searchQuery, ignoreCase = true) ||
            it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    val pinnedUnits = remember(filteredUnits) { filteredUnits.filter { it.isPinned } }
    val otherUnits = remember(filteredUnits) { filteredUnits.filter { !it.isPinned } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select unit",
                color = colors.textMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search units…", color = colors.textDim) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = colors.textDim,
                    )
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            ) {
                if (pinnedUnits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Common",
                            color = Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    items(pinnedUnits, key = { it.id }) { unit ->
                        UnitRow(
                            unit = unit,
                            isSelected = unit.id == selectedUnit.id,
                            onClick = {
                                onSelectUnit(unit)
                                onDismiss()
                            },
                        )
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }
                }

                if (otherUnits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Other",
                            color = colors.textDim,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    items(otherUnits, key = { it.id }) { unit ->
                        UnitRow(
                            unit = unit,
                            isSelected = unit.id == selectedUnit.id,
                            onClick = {
                                onSelectUnit(unit)
                                onDismiss()
                            },
                        )
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitRow(
    unit: UnitDef,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = mathlatorColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = unit.nameResKey,
                color = if (isSelected) Primary else colors.textMain,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
        }
        Text(
            text = unit.symbol,
            color = if (isSelected) Primary else colors.textDim,
            fontSize = 14.sp,
        )
    }
}
