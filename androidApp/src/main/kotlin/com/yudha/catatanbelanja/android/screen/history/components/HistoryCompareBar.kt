package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val COMPARE_EMOJI = "⇄"

/** The pinned `.finish` bar shown once two sessions are picked. */
@Composable
internal fun HistoryCompareBar(
    onRunCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.shapes.radius))
            .background(colors.ink)
            .padding(start = 18.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.history_compare_selected),
                style = AppTheme.typography.tiny,
                color = colors.paper.copy(alpha = 0.7f),
            )
            Text(
                text = stringResource(R.string.history_compare_ready),
                style = AppTheme.typography.sectionTitle,
                color = colors.paper,
            )
        }

        AppButton(
            text = stringResource(R.string.history_compare_run),
            onClick = onRunCompare,
            emoji = COMPARE_EMOJI,
            fillWidth = false,
        )
    }
}
