package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppLineChart
import com.yudha.catatanbelanja.android.designsystem.component.display.AppLineChartPoint
import com.yudha.catatanbelanja.android.designsystem.component.display.AppStatCard
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.android.format.toShortDateLabel
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData

/** The line itself, its headline change, and the three numbers that put it in context. */
@Composable
internal fun TrendChartCard(
    data: PriceTrendData,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val points = remember(data.points) {
        data.points.map { point ->
            AppLineChartPoint(
                valueLabel = point.price.toRupiahShort(),
                dateLabel = point.endedAt.toShortDateLabel(),
                ratio = point.ratio,
            )
        }
    }
    // Paying more than the first sample is the bad direction, so it reads coral.
    val deltaColor = when {
        data.isUp -> colors.coral
        data.isDown -> colors.mint
        else -> colors.inkTertiary
    }
    val delta = when (data.isUp) {
        true -> stringResource(R.string.dashboard_trend_percent_up, data.deltaPercent)
        false -> stringResource(R.string.dashboard_trend_percent_down, data.deltaPercent)
    }

    AppCard(modifier = modifier) {
        AppLineChart(points = points)
        Spacer(Modifier.height(Spacing.x8))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x6),
        ) {
            Text(
                text = stringResource(
                    R.string.trend_summary,
                    data.usableCount,
                    data.firstValue.toRupiah(),
                    data.lastValue.toRupiah(),
                ),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.tiny,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = delta, style = AppTheme.typography.fieldLabel, color = deltaColor)
        }
        Spacer(Modifier.height(Spacing.x14))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppStatCard(
                label = stringResource(R.string.trend_stat_cheapest),
                value = data.cheapest.toRupiahShort(),
                modifier = Modifier.weight(1f),
            )
            AppStatCard(
                label = stringResource(R.string.trend_stat_average),
                value = data.average.toRupiahShort(),
                modifier = Modifier.weight(1f),
            )
            AppStatCard(
                label = stringResource(R.string.trend_stat_dearest),
                value = data.dearest.toRupiahShort(),
                modifier = Modifier.weight(1f),
            )
        }

        if (data.skippedCount <= 0) return@AppCard

        Spacer(Modifier.height(Spacing.x12))
        Text(
            text = stringResource(R.string.trend_skipped, data.skippedCount),
            style = AppTheme.typography.tiny,
            color = colors.coral,
        )
    }
}
