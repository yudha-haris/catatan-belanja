package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.domain.model.SessionSummary

/** The two session cards side by side; A wears the primary rule, B the hero's far end. */
@Composable
internal fun CompareHeaderCards(
    sessionA: SessionSummary,
    sessionB: SessionSummary,
    modifier: Modifier = Modifier,
) {
    val dayA = (sessionA.session.endedAt ?: sessionA.session.startedAt).toDayLabel()
    val dayB = (sessionB.session.endedAt ?: sessionB.session.startedAt).toDayLabel()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        CompareSideCard(
            label = stringResource(R.string.compare_side_a, dayA),
            name = sessionA.session.name,
            amount = sessionA.total.toRupiah(),
            itemCountLabel = pluralStringResource(
                R.plurals.common_item_count,
                sessionA.itemCount,
                sessionA.itemCount,
            ),
            accent = AppTheme.colors.primary,
            modifier = Modifier.weight(1f),
        )
        CompareSideCard(
            label = stringResource(R.string.compare_side_b, dayB),
            name = sessionB.session.name,
            amount = sessionB.total.toRupiah(),
            itemCountLabel = pluralStringResource(
                R.plurals.common_item_count,
                sessionB.itemCount,
                sessionB.itemCount,
            ),
            accent = AppTheme.colors.heroEnd,
            modifier = Modifier.weight(1f),
        )
    }
}
