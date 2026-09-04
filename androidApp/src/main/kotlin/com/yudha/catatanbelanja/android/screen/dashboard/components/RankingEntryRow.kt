package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppRankRow
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankedEntry

/**
 * One ranked row. An item bought more than once opens its price trend — the ranking says what
 * costs the most, and the trend is the only place that says whether it is getting worse. A
 * category row, and an item bought once, are inert rather than tappable-with-nothing-behind-them.
 */
@Composable
internal fun RankingEntryRow(
    rank: Int,
    entry: RankedEntry,
    showDivider: Boolean,
    onOpenTrend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hint = when (entry.isOther || entry.trendName.isEmpty()) {
        true -> stringResource(
            R.string.ranking_entry_hint_category,
            entry.purchaseCount,
            entry.sharePercent,
        )
        false -> stringResource(
            R.string.ranking_entry_hint_item,
            entry.purchaseCount,
            entry.averagePrice.toRupiahShort(),
            entry.sharePercent,
        )
    }
    val tap = when (entry.canOpenTrend) {
        true -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onOpenTrend(entry.trendName) },
        )
        false -> Modifier
    }

    AppRankRow(
        rank = rank,
        emoji = entry.emoji,
        title = when (entry.isOther) {
            true -> stringResource(R.string.ranking_other)
            false -> entry.label
        },
        valueLabel = entry.total.toRupiah(),
        ratio = entry.ratio,
        // The hint stays the numbers even on a tappable row: "3× · Rp12.000 each" is worth more
        // than a label repeating what the card header already said about tapping.
        hint = hint,
        modifier = modifier.then(tap),
        showDivider = showDivider,
    )
}
