package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader

private const val EMPTY_EMOJI = "🗂️"

/** Header plus the "Belum ada riwayat" state with the demo-data shortcut. */
@Composable
internal fun HistoryEmptyContent(
    onOpenSettings: () -> Unit,
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppScreenHeader(
            title = stringResource(R.string.history_title),
            actions = {
                AppIconButton(
                    onClick = onOpenSettings,
                    contentDescription = stringResource(R.string.common_cd_settings),
                    icon = Icons.Rounded.Settings,
                )
            },
        )

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
        )
    }
}
