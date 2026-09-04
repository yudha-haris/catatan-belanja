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
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.android.format.toShortDateLabel
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData

/** "Tren harga": pick an item bought at least twice and watch its price move. */
@Composable
internal fun DashboardTrendCard(
    data: DashboardData,
    onSelectTrendItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = remember(data.trendPoints) {
        data.trendPoints.map { point ->
            AppLineChartPoint(
                valueLabel = point.price.toRupiahShort(),
                dateLabel = point.endedAt.toShortDateLabel(),
                ratio = point.ratio,
            )
        }
    }
    val colors = AppTheme.colors
    // A dearer item than the first sample is the bad direction, so it reads coral.
    val deltaColor = when {
        data.isTrendUp -> colors.coral
        data.isTrendDown -> colors.mint
        else -> colors.inkTertiary
    }
    val delta = when (data.isTrendUp) {
        true -> stringResource(R.string.dashboard_trend_percent_up, data.trendDeltaPercent)
        false -> stringResource(R.string.dashboard_trend_percent_down, data.trendDeltaPercent)
    }

    AppCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.dashboard_trend_title),
            style = AppTheme.typography.sectionTitle,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.x10))
        AppUnitDropdown(
            value = data.trendName.orEmpty(),
            onValueChange = onSelectTrendItem,
            units = data.trendableNames,
        )

        if (!data.hasTrend) return@AppCard

        Spacer(Modifier.height(Spacing.x8))
        AppLineChart(points = points)
        Spacer(Modifier.height(Spacing.x6))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x4),
        ) {
            Text(
                text = stringResource(
                    R.string.dashboard_trend_summary,
                    data.trendPoints.size,
                    data.trendFirstPrice.toRupiah(),
                    data.trendLastPrice.toRupiah(),
                ),
                style = AppTheme.typography.tiny,
            )
            Text(text = delta, style = AppTheme.typography.fieldLabel, color = deltaColor)
        }
    }
}
