package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppStatCard
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingReportData

/** What the chosen window adds up to: the total, the trips behind it and the biggest single one. */
@Composable
internal fun ReportStatsGrid(
    data: SpendingReportData,
    onOpenBiggestTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppStatCard(
                label = stringResource(R.string.report_stat_total),
                value = data.total.toRupiahShort(),
                modifier = Modifier.weight(1f),
                hint = stringResource(
                    R.string.report_stat_month_average,
                    data.monthAverage.toRupiahShort(),
                ),
            )
            AppStatCard(
                label = stringResource(R.string.report_stat_trips),
                value = stringResource(R.string.report_stat_trips_value, data.tripCount),
                modifier = Modifier.weight(1f),
                hint = "${data.tripAverage.toRupiahShort()} ${stringResource(R.string.report_stat_trip_average)}",
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
            AppStatCard(
                label = stringResource(R.string.report_stat_highest),
                value = data.highestTotal.toRupiah(),
                modifier = Modifier.weight(1f),
                hint = stringResource(R.string.report_stat_highest_hint),
                onClick = when (data.hasHighest) {
                    true -> ({ onOpenBiggestTrip(data.highestSessionId) })
                    false -> null
                },
            )
            AppStatCard(
                label = stringResource(R.string.report_stat_months),
                value = stringResource(R.string.report_stat_months_value, data.monthCount),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
