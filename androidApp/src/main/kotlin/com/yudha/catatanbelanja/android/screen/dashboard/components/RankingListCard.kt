package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankedEntry
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankingMode

/** The whole ranking, not the top five. */
@Composable
internal fun RankingListCard(
    entries: List<RankedEntry>,
    mode: RankingMode,
    onOpenTrend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.ranking_list_title),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when (mode) {
                    RankingMode.ITEM -> stringResource(R.string.ranking_trend_cta)
                    RankingMode.CATEGORY -> stringResource(R.string.ranking_list_hint, entries.size)
                },
                style = AppTheme.typography.tiny,
            )
        }
        Spacer(Modifier.height(Spacing.x6))

        entries.forEachIndexed { index, entry ->
            RankingEntryRow(
                rank = index + 1,
                entry = entry,
                showDivider = index < entries.lastIndex,
                onOpenTrend = onOpenTrend,
            )
        }
    }
}
