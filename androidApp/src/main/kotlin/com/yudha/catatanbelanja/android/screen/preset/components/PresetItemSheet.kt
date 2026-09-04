package com.yudha.catatanbelanja.android.screen.preset.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.model.CatalogItem

/**
 * Add or edit one catalog item: its name, the category it is filed under, and the unit the add
 * form should reach for first. Picking a different category here is how an item is moved.
 */
@Composable
internal fun PresetItemSheet(
    item: CatalogItem?,
    categories: List<CatalogCategory>,
    selectedCategoryId: String,
    unit: String,
    units: List<String>,
    onPickCategory: (String) -> Unit,
    onPickUnit: (String) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (CatalogItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var name by remember(item?.id) { mutableStateOf(item?.name.orEmpty()) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = when (item) {
                null -> stringResource(R.string.preset_items_sheet_add_title)
                else -> stringResource(R.string.preset_items_sheet_edit_title)
            },
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.common_item_name_label),
            placeholder = stringResource(R.string.preset_items_name_placeholder),
            enabled = enabled,
        )

        Spacer(Modifier.height(Spacing.x14))
        Text(
            text = stringResource(R.string.preset_items_category_label),
            style = AppTheme.typography.fieldLabel,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x8))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            categories.forEach { category ->
                AppChip(
                    text = category.name,
                    onClick = { onPickCategory(category.id) },
                    emoji = category.emoji,
                    selected = category.id == selectedCategoryId,
                    enabled = enabled,
                )
            }
        }

        Spacer(Modifier.height(Spacing.x14))
        // "no default unit" is an option in the menu, not just a placeholder — otherwise an item
        // given a unit by mistake could never be put back to having none.
        val noUnitLabel = stringResource(R.string.preset_items_no_default_unit)
        AppUnitDropdown(
            value = unit.ifEmpty { noUnitLabel },
            onValueChange = { picked -> onPickUnit(if (picked == noUnitLabel) "" else picked) },
            units = listOf(noUnitLabel) + units,
            label = stringResource(R.string.preset_items_default_unit_label),
            enabled = enabled,
        )

        Spacer(Modifier.height(Spacing.x20))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        ) {
            if (item != null) {
                AppButton(
                    text = stringResource(R.string.common_delete),
                    onClick = { onDelete(item) },
                    modifier = Modifier.weight(1f),
                    variant = AppButtonVariant.Danger,
                    enabled = enabled,
                )
            }
            AppButton(
                text = stringResource(R.string.common_save),
                onClick = { onSave(name) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
        }
    }
}
