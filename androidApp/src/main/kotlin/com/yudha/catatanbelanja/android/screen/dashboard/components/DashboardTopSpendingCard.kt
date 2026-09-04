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
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppRankRow
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.model.TopItem

/** "Pengeluaran terbesar": the scope chips plus the top five items of the chosen scope. */
@Composable
internal fun DashboardTopSpendingCard(
    topItems: List<TopItem>,
    scope: DashboardScope,
    onSelectScope: (DashboardScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.dashboard_top_spending),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            AppChip(
                text = stringResource(R.string.dashboard_scope_month),
                onClick = { onSelectScope(DashboardScope.MONTH) },
                selected = scope == DashboardScope.MONTH,
                variant = AppChipVariant.Plain,
            )
            AppChip(
                text = stringResource(R.string.dashboard_scope_all),
                onClick = { onSelectScope(DashboardScope.ALL) },
                selected = scope == DashboardScope.ALL,
                variant = AppChipVariant.Plain,
            )
        }
        Spacer(Modifier.height(Spacing.x6))

        topItems.forEachIndexed { index, item ->
            AppRankRow(
                rank = index + 1,
                emoji = item.emoji,
                title = item.name,
                valueLabel = item.total.toRupiah(),
                ratio = item.ratio,
                hint = stringResource(R.string.dashboard_rank_hint, item.purchaseCount, item.sharePercent),
                showDivider = index < topItems.lastIndex,
            )
        }
    }
}
