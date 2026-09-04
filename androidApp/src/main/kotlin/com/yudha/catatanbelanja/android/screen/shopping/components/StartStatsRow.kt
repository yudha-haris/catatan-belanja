package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppStatCard
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiahShort

/** The `.stats` pair: what this month cost so far, and what an average trip costs. */
@Composable
internal fun StartStatsRow(
    monthTotal: Int,
    monthCount: Int,
    monthAverage: Int,
    modifier: Modifier = Modifier,
) {
    val average = when (monthCount) {
        0 -> stringResource(R.string.common_empty_value)
        else -> monthAverage.toRupiahShort()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        AppStatCard(
            label = stringResource(R.string.home_stat_month_label),
            value = monthTotal.toRupiahShort(),
            modifier = Modifier.weight(1f),
            hint = pluralStringResource(R.plurals.home_stat_month_hint, monthCount, monthCount),
        )
        AppStatCard(
            label = stringResource(R.string.home_stat_average_label),
            value = average,
            modifier = Modifier.weight(1f),
            hint = stringResource(R.string.home_stat_average_hint),
        )
    }
}
