package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppStatCard
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData

/** The `.stats` grid: this month's spend against last month, and how many trips it took. */
@Composable
internal fun DashboardStatsRow(
    data: DashboardData,
    modifier: Modifier = Modifier,
) {
    // Spending more than last month is bad news, so Up is coral and Down mint.
    val comparisonTone = when {
        data.isMonthSpendingUp -> AppBadgeTone.Up
        data.isMonthSpendingDown -> AppBadgeTone.Down
        else -> AppBadgeTone.Neutral
    }
    val arrow = if (data.isMonthSpendingDown) "▼" else "▲"
    val comparison = when (data.hasMonthComparison) {
        true -> "$arrow " + stringResource(R.string.dashboard_vs_last_month, data.monthDeltaPercent)
        false -> stringResource(R.string.dashboard_no_comparison)
    }
    val average = when (data.hasMonthAverage) {
        true -> data.monthAverage.toRupiahShort()
        false -> stringResource(R.string.common_empty_value)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        AppStatCard(
            label = stringResource(R.string.dashboard_stat_month),
            value = data.monthTotal.toRupiahShort(),
            modifier = Modifier.weight(1f),
            hint = comparison,
            hintTone = comparisonTone,
        )
        AppStatCard(
            label = stringResource(R.string.dashboard_stat_sessions),
            value = stringResource(R.string.dashboard_stat_sessions_value, data.monthSessionCount),
            modifier = Modifier.weight(1f),
            hint = stringResource(R.string.dashboard_stat_average, average),
        )
    }
}
