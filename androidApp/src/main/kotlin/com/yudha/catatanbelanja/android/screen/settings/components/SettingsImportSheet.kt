package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.Spacer
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

/**
 * Either half works on its own: [onPickFile] hands the picked document to the screen, and the
 * paste box submits its buffer through [onSubmit]. The merge never overwrites, it only adds.
 */
@Composable
internal fun SettingsImportSheet(
    onPickFile: () -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var pasted by remember { mutableStateOf("") }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_import_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = stringResource(R.string.settings_import_sheet_message),
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x20))
        AppButton(
            text = stringResource(R.string.settings_import_pick_file),
            onClick = onPickFile,
            variant = AppButtonVariant.Soft,
            emoji = "📂",
            big = true,
            enabled = enabled,
        )
        Spacer(Modifier.height(Spacing.x16))
        AppTextField(
            value = pasted,
            onValueChange = { pasted = it },
            label = stringResource(R.string.settings_import_paste_label),
            placeholder = stringResource(R.string.settings_import_paste_placeholder),
            enabled = enabled,
            singleLine = false,
            maxLines = 6,
        )
        Spacer(Modifier.height(Spacing.x16))
        AppButton(
            text = stringResource(R.string.settings_import_confirm),
            onClick = { onSubmit(pasted) },
            big = true,
            enabled = enabled && pasted.isNotBlank(),
        )
    }
}
