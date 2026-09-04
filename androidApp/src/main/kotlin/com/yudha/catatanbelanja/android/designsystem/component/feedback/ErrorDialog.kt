package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.core.common.Failure

/** Localized error surface. [Failure.message] is developer-facing, shown only as detail. */
@Composable
fun ErrorDialog(
    failure: Failure,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDialog(
        title = stringResource(R.string.common_error_title),
        onDismiss = onDismiss,
        modifier = modifier,
        emoji = "😕",
        message = stringResource(R.string.common_error_message),
        detail = failure.message.ifBlank { null },
    ) {
        AppButton(
            text = stringResource(R.string.common_ok),
            onClick = onDismiss,
            variant = AppButtonVariant.Ghost,
        )
    }
}
