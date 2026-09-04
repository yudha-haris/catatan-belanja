package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val EMPTY_EMOJI = "🗂️"
private const val SCAN_EMOJI = "🧾"

/**
 * The "Belum ada riwayat" state. Someone with an empty history and a drawer full of old receipts
 * is exactly who the scanner is for, so it leads here and the demo data steps back to a ghost.
 */
@Composable
internal fun HistoryEmptyContent(
    onScanReceipt: () -> Unit,
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppEmptyState(
        emoji = EMPTY_EMOJI,
        title = stringResource(R.string.history_empty_title),
        message = stringResource(R.string.history_empty_message),
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
                AppButton(
                    text = stringResource(R.string.history_scan_cta),
                    onClick = onScanReceipt,
                    emoji = SCAN_EMOJI,
                    fillWidth = false,
                )
                AppButton(
                    text = stringResource(R.string.common_try_demo),
                    onClick = onSeedDemo,
                    variant = AppButtonVariant.Ghost,
                    fillWidth = false,
                )
            }
        },
        modifier = modifier,
    )
}
