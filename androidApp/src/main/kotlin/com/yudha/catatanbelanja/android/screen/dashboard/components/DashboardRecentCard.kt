package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBarChart
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBarChartBar
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.android.format.toShortDateLabel
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingBar

/** "8 belanja terakhir" — tapping a bar opens that session's receipt. */
@Composable
internal fun DashboardRecentCard(
    bars: List<SpendingBar>,
    onOpenSessionDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chartBars = remember(bars) {
        bars.map { bar ->
            AppBarChartBar(
                label = bar.endedAt.toShortDateLabel(),
                valueLabel = bar.total.toRupiahShort(),
                ratio = bar.ratio,
                highlighted = bar.isHighest,
            )
        }
    }

    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.dashboard_recent_title),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.dashboard_recent_hint),
                style = AppTheme.typography.tiny,
            )
        }
        AppBarChart(
            bars = chartBars,
            onBarClick = { index -> onOpenSessionDetail(bars[index].sessionId) },
        )
        DashboardSeeAllRow(
            text = stringResource(R.string.dashboard_see_all_trips),
            onClick = onSeeAll,
        )
    }
}
