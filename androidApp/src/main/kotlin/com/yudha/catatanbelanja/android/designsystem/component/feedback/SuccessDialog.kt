package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDialog(
        title = message,
        onDismiss = onDismiss,
        modifier = modifier,
        emoji = "✅",
    ) {
        AppButton(
            text = stringResource(R.string.common_ok),
            onClick = onDismiss,
            variant = AppButtonVariant.Primary,
        )
    }
}
