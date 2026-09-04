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
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData

/**
 * "Tren harga": pick an item bought at least twice and watch its price move.
 *
 * [trend] comes from the same use case the trend page uses, so an item the user switched to a unit
 * price over there is drawn as a unit price here too — a card that quietly disagreed with the page
 * it links to would be worse than no card.
 */
@Composable
internal fun DashboardTrendCard(
    trend: PriceTrendData,
    names: List<String>,
    onSelectTrendItem: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = remember(trend.points) {
        trend.points.map { point ->
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
        trend.isUp -> colors.coral
        trend.isDown -> colors.mint
        else -> colors.inkTertiary
    }
    val delta = when (trend.isUp) {
        true -> stringResource(R.string.dashboard_trend_percent_up, trend.deltaPercent)
        false -> stringResource(R.string.dashboard_trend_percent_down, trend.deltaPercent)
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
            value = trend.name,
            onValueChange = onSelectTrendItem,
            units = names,
        )

        if (!trend.hasTrend) {
            DashboardSeeAllRow(
                text = stringResource(R.string.dashboard_see_all_trend),
                onClick = onSeeAll,
            )
            return@AppCard
        }

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
                    trend.usableCount,
                    trend.firstValue.toRupiah(),
                    trend.lastValue.toRupiah(),
                ),
                style = AppTheme.typography.tiny,
            )
            Text(text = delta, style = AppTheme.typography.fieldLabel, color = deltaColor)
        }
        DashboardSeeAllRow(
            text = stringResource(R.string.dashboard_see_all_trend),
            onClick = onSeeAll,
        )
    }
}
