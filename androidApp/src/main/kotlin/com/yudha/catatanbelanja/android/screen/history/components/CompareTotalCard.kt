package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadge
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiahSigned
import com.yudha.catatanbelanja.features.history.domain.model.CompareResult

private const val ARROW_UP = "▲"
private const val ARROW_DOWN = "▼"

/** "Selisih total (B − A)" with the naik / turun tally. Spending more is the bad direction. */
@Composable
internal fun CompareTotalCard(
    result: CompareResult,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val deltaColor = when {
        result.delta > 0 -> colors.coral
        result.delta < 0 -> colors.mint
        else -> colors.ink
    }
    val percentLabel = when (result.deltaPercent > 0) {
        true -> stringResource(R.string.compare_delta_percent_up, result.deltaPercent)
        false -> stringResource(R.string.compare_delta_percent_down, result.deltaPercent)
    }

    AppCard(modifier = modifier, flat = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.compare_delta_label),
                    style = AppTheme.typography.tiny,
                    color = colors.inkTertiary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${result.delta.toRupiahSigned()} $percentLabel",
                    style = AppTheme.typography.sectionTitle,
                    color = deltaColor,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x6)) {
                AppBadge(
                    text = "$ARROW_UP ${stringResource(R.string.compare_badge_up, result.upCount)}",
                    tone = AppBadgeTone.Up,
                )
                AppBadge(
                    text = "$ARROW_DOWN ${stringResource(R.string.compare_badge_down, result.downCount)}",
                    tone = AppBadgeTone.Down,
                )
            }
        }
    }
}
