package com.yudha.catatanbelanja.android.screen.preset.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.BrandPreset

/** Add or edit one brand. [brand] null means adding, which is what hides the delete button. */
@Composable
internal fun PresetBrandSheet(
    brand: BrandPreset?,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // Keyed on the row: reopening the sheet on another brand starts from that brand's name.
    var name by remember(brand?.id) { mutableStateOf(brand?.name.orEmpty()) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = when (brand) {
                null -> stringResource(R.string.preset_brands_sheet_add_title)
                else -> stringResource(R.string.preset_brands_sheet_edit_title)
            },
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.preset_brands_name_label),
            placeholder = stringResource(R.string.preset_brands_name_placeholder),
            enabled = enabled,
        )

        Spacer(Modifier.height(Spacing.x20))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        ) {
            if (brand != null) {
                AppButton(
                    text = stringResource(R.string.common_delete),
                    onClick = { onDelete(brand.id) },
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
