package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** One half of the `.cmp-head` grid: a card capped by a 4dp side-coloured rule. */
@Composable
internal fun CompareSideCard(
    label: String,
    name: String,
    amount: String,
    itemCountLabel: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier, contentPadding = PaddingValues()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(accent),
        )
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = AppTheme.typography.tiny,
                color = AppTheme.colors.inkTertiary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = name,
                style = AppTheme.typography.rowTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Spacing.x6))
            Text(
                text = amount,
                style = AppTheme.typography.statValue,
                color = AppTheme.colors.ink,
                maxLines = 1,
            )
            Text(
                text = itemCountLabel,
                style = AppTheme.typography.tiny,
                color = AppTheme.colors.inkTertiary,
                maxLines = 1,
            )
        }
    }
}
