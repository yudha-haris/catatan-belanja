package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppMoneyField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem

/** The prototype's `itemSheet()` reached from a finished session: edit or delete one item. */
@Composable
internal fun SessionDetailItemSheet(
    item: ShoppingItem,
    emoji: String,
    onSave: (name: String, qtyText: String, unit: String, note: String, priceText: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var qtyText by remember(item.id) { mutableStateOf(item.qty.toQtyLabel()) }
    var unit by remember(item.id) { mutableStateOf(item.unit ?: CatalogData.units.first()) }
    var price by remember(item.id) { mutableIntStateOf(item.price) }
    var note by remember(item.id) { mutableStateOf(item.note) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = "$emoji ${item.name}",
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.common_item_name_label),
        )
        Spacer(Modifier.height(Spacing.x12))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppTextField(
                value = qtyText,
                onValueChange = { qtyText = it },
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.common_qty_label),
                placeholder = stringResource(R.string.common_item_qty_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AppUnitDropdown(
                value = unit,
                onValueChange = { unit = it },
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.common_unit_label),
            )
        }
        Spacer(Modifier.height(Spacing.x12))

        AppMoneyField(
            value = price,
            onValueChange = { price = it },
            label = stringResource(R.string.common_price_label),
        )
        Spacer(Modifier.height(Spacing.x12))

        AppTextField(
            value = note,
            onValueChange = { note = it },
            label = stringResource(R.string.common_item_note_label),
            optionalLabel = stringResource(R.string.common_optional),
            placeholder = stringResource(R.string.common_item_note_placeholder),
        )
        Spacer(Modifier.height(Spacing.x20))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppButton(
                text = stringResource(R.string.common_delete),
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                variant = AppButtonVariant.Danger,
            )
            AppButton(
                text = stringResource(R.string.common_save),
                onClick = { onSave(name, qtyText, unit, note, price.toString()) },
                modifier = Modifier.weight(2f),
                enabled = name.isNotBlank(),
            )
        }
    }
}
