package com.yudha.catatanbelanja.android.screen.preset.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory

/** Add or edit one category: its name, and the emoji every item under it is drawn with. */
@Composable
internal fun PresetCategorySheet(
    category: CatalogCategory?,
    onSave: (String, String) -> Unit,
    onDelete: (CatalogCategory) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var emoji by remember(category?.id) { mutableStateOf(category?.emoji.orEmpty()) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = when (category) {
                null -> stringResource(R.string.preset_categories_sheet_add_title)
                else -> stringResource(R.string.preset_categories_sheet_edit_title)
            },
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            AppTextField(
                value = emoji,
                onValueChange = { emoji = it },
                modifier = Modifier.width(96.dp),
                label = stringResource(R.string.preset_categories_emoji_label),
                placeholder = "🛍️",
                enabled = enabled,
            )
            Spacer(Modifier.width(Spacing.x10))
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.preset_categories_name_label),
                placeholder = stringResource(R.string.preset_categories_name_placeholder),
                enabled = enabled,
            )
        }

        Spacer(Modifier.height(Spacing.x20))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        ) {
            if (category != null) {
                AppButton(
                    text = stringResource(R.string.common_delete),
                    onClick = { onDelete(category) },
                    modifier = Modifier.weight(1f),
                    variant = AppButtonVariant.Danger,
                    enabled = enabled,
                )
            }
            AppButton(
                text = stringResource(R.string.common_save),
                onClick = { onSave(name, emoji) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
        }
    }
}
