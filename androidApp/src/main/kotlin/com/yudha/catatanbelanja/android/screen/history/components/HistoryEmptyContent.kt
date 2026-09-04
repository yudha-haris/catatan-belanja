package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState

private const val EMPTY_EMOJI = "🗂️"

/** The "Belum ada riwayat" state with the demo-data shortcut. */
@Composable
internal fun HistoryEmptyContent(
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppEmptyState(
        emoji = EMPTY_EMOJI,
        title = stringResource(R.string.history_empty_title),
        message = stringResource(R.string.history_empty_message),
        action = {
            AppButton(
                text = stringResource(R.string.common_try_demo),
                onClick = onSeedDemo,
                variant = AppButtonVariant.Ghost,
                fillWidth = false,
            )
        },
        modifier = modifier,
    )
}
