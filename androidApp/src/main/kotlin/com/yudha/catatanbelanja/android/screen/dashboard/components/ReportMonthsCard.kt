package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBarChart
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBarChartBar
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToShortLabel
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.dashboard.domain.model.MonthSpending

/** The shape of the window: a bar per recent month, then every month in it as a row. */
@Composable
internal fun ReportMonthsCard(
    bars: List<MonthSpending>,
    months: List<MonthSpending>,
    modifier: Modifier = Modifier,
) {
    val chartBars = remember(bars) {
        bars.map { month ->
            AppBarChartBar(
                label = month.monthKey.monthKeyToShortLabel(),
                valueLabel = month.total.toRupiahShort(),
                ratio = month.ratio,
                highlighted = month.isHighest,
            )
        }
    }

    AppCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.report_months_title),
            style = AppTheme.typography.sectionTitle,
            color = AppTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AppBarChart(bars = chartBars)
        Spacer(Modifier.height(Spacing.x8))

        months.forEach { month ->
            ReportMonthRow(month = month)
            Spacer(Modifier.height(Spacing.x6))
        }
    }
}
