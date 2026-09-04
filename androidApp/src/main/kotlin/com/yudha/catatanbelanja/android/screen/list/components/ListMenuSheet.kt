package com.yudha.catatanbelanja.android.screen.list.components

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

/** The header's "⋯": keep this list for next time, or throw it away. */
@Composable
internal fun ListMenuSheet(
    onSaveTemplate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.list_menu_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppButton(
            text = stringResource(R.string.list_menu_save_template),
            onClick = onSaveTemplate,
            variant = AppButtonVariant.Ghost,
            emoji = "⭐",
        )
        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = stringResource(R.string.list_menu_delete),
            onClick = onDelete,
            variant = AppButtonVariant.Danger,
            emoji = "🗑",
        )
    }
}
