package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState

/** Shown until the first session is finished; the ghost button seeds the demo data. */
@Composable
internal fun DashboardEmptyState(
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppEmptyState(
        emoji = "📊",
        title = stringResource(R.string.dashboard_empty_title),
        message = stringResource(R.string.dashboard_empty_message),
        modifier = modifier,
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
