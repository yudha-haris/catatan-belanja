package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

@Composable
fun ConfirmationBottomSheet(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.common_cancel),
    isDanger: Boolean = false,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = title,
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = message,
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x20))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppButton(
                text = cancelText,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = AppButtonVariant.Soft,
            )
            AppButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = if (isDanger) AppButtonVariant.Danger else AppButtonVariant.Primary,
            )
        }
    }
}
