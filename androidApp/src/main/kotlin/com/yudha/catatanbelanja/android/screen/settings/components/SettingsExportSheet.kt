package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** Two ways out of the app with the same JSON: the system share sheet, or the clipboard. */
@Composable
internal fun SettingsExportSheet(
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_export_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = stringResource(R.string.settings_export_sheet_message),
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x20))
        AppButton(
            text = stringResource(R.string.settings_export_share),
            onClick = onShare,
            emoji = "📤",
            big = true,
            enabled = enabled,
        )
        Spacer(Modifier.height(Spacing.x10))
        AppButton(
            text = stringResource(R.string.settings_export_copy),
            onClick = onCopy,
            variant = AppButtonVariant.Soft,
            emoji = "📋",
            big = true,
            enabled = enabled,
        )
    }
}
