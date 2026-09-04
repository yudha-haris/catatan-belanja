package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val COMPARE_EMOJI = "⇄"
private const val REPEAT_EMOJI = "🔁"

/** "Bandingkan" only exists once there is a second session; alone, "Belanja lagi" spans the row. */
@Composable
internal fun SessionDetailActions(
    canCompare: Boolean,
    onOpenComparePicker: () -> Unit,
    onRepeatSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!canCompare) {
        AppButton(
            text = stringResource(R.string.detail_repeat_button_long),
            onClick = onRepeatSession,
            modifier = modifier.padding(bottom = Spacing.x16),
            variant = AppButtonVariant.Ghost,
            emoji = REPEAT_EMOJI,
        )
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.x16),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        AppButton(
            text = stringResource(R.string.detail_compare_button),
            onClick = onOpenComparePicker,
            modifier = Modifier.weight(1f),
            variant = AppButtonVariant.Ghost,
            emoji = COMPARE_EMOJI,
        )
        AppButton(
            text = stringResource(R.string.detail_repeat_button),
            onClick = onRepeatSession,
            modifier = Modifier.weight(1f),
            variant = AppButtonVariant.Ghost,
            emoji = REPEAT_EMOJI,
        )
    }
}
