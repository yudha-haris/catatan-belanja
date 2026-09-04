package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow

/**
 * The add / edit stock sheet. Every field is a local buffer that is only pushed on Simpan, which
 * is why the sheet re-keys its buffers on the edited item and on the unit the ViewModel guesses.
 */
@Composable
internal fun StockEditorSheet(
    isNew: Boolean,
    item: StockItem?,
    emoji: String,
    unit: String,
    units: List<String>,
    knownNames: List<String>,
    shadow: StockShadow?,
    rateMode: RateMode,
    autoEstimate: StockRateEstimate?,
    enabled: Boolean,
    onNameChanged: (String) -> Unit,
    onSave: (name: String, qtyText: String, unit: String, minText: String) -> Unit,
    onMarkEmpty: () -> Unit,
    onOpenRate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var qtyText by remember(item?.id) { mutableStateOf(item?.qty.toQtyLabel()) }
    var minText by remember(item?.id) { mutableStateOf(item?.minQty.toQtyLabel()) }
    var selectedUnit by remember(unit) { mutableStateOf(unit) }
    val nameFocus = remember { FocusRequester() }

    // The prototype focuses the name field the moment a blank sheet opens.
    LaunchedEffect(isNew) {
        if (!isNew) return@LaunchedEffect
        nameFocus.requestFocus()
    }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = when {
                isNew -> stringResource(R.string.stock_sheet_add_title)
                else -> "$emoji ${item?.name.orEmpty()}"
            },
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = {
                name = it
                onNameChanged(it)
            },
            modifier = Modifier.focusRequester(nameFocus),
            label = stringResource(R.string.common_item_name_label),
            placeholder = stringResource(R.string.stock_sheet_name_placeholder),
            enabled = enabled,
        )

        // Stands in for the prototype's <datalist>: the known names, offered while the field is blank.
        if (name.isEmpty() && knownNames.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.x10))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.x8)) {
                items(knownNames, key = { it }) { known ->
                    AppChip(
                        text = known,
                        onClick = {
                            name = known
                            onNameChanged(known)
                        },
                        variant = AppChipVariant.Plain,
                        enabled = enabled,
                    )
                }
            }
        }

        if (shadow != null) {
            Spacer(Modifier.height(Spacing.x14))
            StockSmartCard(
                shadow = shadow,
                enabled = enabled,
                // Types the estimate into the field below and stops there. Simpan is still the
                // user's tap, so an estimate never becomes a saved quantity on its own.
                onApply = { qtyText = shadow.estimatedQty.toQtyLabel() },
            )
        }

        Spacer(Modifier.height(Spacing.x12))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppTextField(
                value = qtyText,
                onValueChange = { qtyText = it },
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.stock_sheet_qty_label),
                placeholder = stringResource(R.string.stock_sheet_qty_placeholder),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AppUnitDropdown(
                value = selectedUnit,
                onValueChange = { selectedUnit = it },
                modifier = Modifier.weight(1f),
                units = units,
                label = stringResource(R.string.common_unit_label),
                enabled = enabled,
            )
        }

        Spacer(Modifier.height(Spacing.x12))
        AppTextField(
            value = minText,
            onValueChange = { minText = it },
            label = stringResource(R.string.stock_sheet_min_label),
            optionalLabel = stringResource(R.string.common_optional),
            placeholder = stringResource(R.string.stock_sheet_min_placeholder),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        // A brand-new row has no history to learn from and no id to hang a rate on yet, so the
        // add sheet stays exactly as short as it has always been.
        if (!isNew) {
            Spacer(Modifier.height(Spacing.x12))
            StockRateRow(
                mode = rateMode,
                shadow = shadow,
                autoEstimate = autoEstimate,
                enabled = enabled,
                onClick = onOpenRate,
            )
        }

        Spacer(Modifier.height(Spacing.x20))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        ) {
            if (!isNew) {
                AppButton(
                    text = stringResource(R.string.common_delete),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    variant = AppButtonVariant.Danger,
                    enabled = enabled,
                )
                AppButton(
                    text = stringResource(R.string.stock_sheet_mark_empty),
                    onClick = onMarkEmpty,
                    modifier = Modifier.weight(1f),
                    variant = AppButtonVariant.Soft,
                    enabled = enabled,
                )
            }
            AppButton(
                text = stringResource(R.string.common_save),
                onClick = { onSave(name, qtyText, selectedUnit, minText) },
                modifier = Modifier.weight(2f),
                enabled = enabled,
            )
        }
    }
}
