package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.dashboard.domain.model.MonthSpending

/** One month of the report: what it cost, how many trips it took, and which way it moved. */
@Composable
internal fun ReportMonthRow(
    month: MonthSpending,
    modifier: Modifier = Modifier,
) {
    // Spending more than the month before is bad news, so Up is coral and Down mint.
    val tone = when {
        month.isUp -> AppBadgeTone.Up
        month.isDown -> AppBadgeTone.Down
        else -> AppBadgeTone.Neutral
    }
    val delta = when {
        !month.hasDelta -> stringResource(R.string.report_month_delta_none)
        month.isDown -> stringResource(R.string.report_month_delta_down, month.deltaPercent)
        else -> stringResource(R.string.report_month_delta_up, month.deltaPercent)
    }

    AppListRow(
        title = month.monthKey.monthKeyToLabel(),
        modifier = modifier,
        subtitle = stringResource(
            R.string.report_month_hint,
            month.sessionCount,
            month.average.toRupiahShort(),
        ),
        trailing = month.total.toRupiah(),
        trailingSub = delta,
        trailingSubTone = tone,
        progress = month.ratio,
    )
}
