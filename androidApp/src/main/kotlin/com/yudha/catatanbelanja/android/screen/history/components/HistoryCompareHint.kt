package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The tinted `.hint` bar: "Pilih 2 sesi…" plus the "2 terakhir" shortcut. */
@Composable
internal fun HistoryCompareHint(
    pickedCount: Int,
    canQuickCompare: Boolean,
    onQuickCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.x12)
            .clip(RoundedCornerShape(AppTheme.shapes.radiusSmall))
            .background(AppTheme.colors.tint)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.history_compare_hint, pickedCount),
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.muted,
            color = AppTheme.colors.primaryDark,
        )

        if (!canQuickCompare) return@Row

        // `selected` is the design system's primary-filled pill — the prototype's hint button.
        AppChip(
            text = stringResource(R.string.history_compare_quick),
            onClick = onQuickCompare,
            selected = true,
        )
    }
}
