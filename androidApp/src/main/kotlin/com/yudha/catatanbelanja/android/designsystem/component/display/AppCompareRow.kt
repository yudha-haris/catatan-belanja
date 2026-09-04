package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * The `.cmp-row` three-column grid. [subOnTop] puts the small sub line above the value, which is
 * how the stock-check log labels its "beli" / "sisa" columns.
 */
@Composable
fun AppCompareRow(
    title: String,
    emoji: String,
    leftLabel: String,
    modifier: Modifier = Modifier,
    leftSub: String? = null,
    rightLabel: String? = null,
    rightSub: String? = null,
    deltaLabel: String? = null,
    deltaTone: AppBadgeTone = AppBadgeTone.Neutral,
    subOnTop: Boolean = false,
    showDivider: Boolean = true,
) {
    val colors = AppTheme.colors
    val deltaColor = when (deltaTone) {
        AppBadgeTone.Tint -> colors.primaryDark
        AppBadgeTone.Up -> colors.coral
        AppBadgeTone.Down -> colors.mint
        AppBadgeTone.Neutral -> colors.inkTertiary
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$emoji $title",
                modifier = Modifier.weight(1.4f),
                style = AppTheme.typography.rowTitle,
                color = colors.ink,
                // Item names are the one column that genuinely runs long ("Minyak Goreng",
                // "Bawang Merah"), and the prices beside them are what the row is for — so wrap
                // to a second line rather than ellipsing the name away.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                if (subOnTop && leftSub != null) {
                    Text(
                        text = leftSub,
                        style = AppTheme.typography.priceDelta,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
                Text(
                    text = leftLabel,
                    style = AppTheme.typography.price,
                    color = colors.ink,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                if (!subOnTop && leftSub != null) {
                    Text(
                        text = leftSub,
                        style = AppTheme.typography.priceDelta,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                if (subOnTop && rightSub != null) {
                    Text(
                        text = rightSub,
                        style = AppTheme.typography.priceDelta,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
                if (rightLabel != null) {
                    Text(
                        text = rightLabel,
                        style = AppTheme.typography.price,
                        color = colors.ink,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
                if (!subOnTop && rightSub != null) {
                    Text(
                        text = rightSub,
                        style = AppTheme.typography.priceDelta,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
                if (deltaLabel != null) {
                    Text(
                        text = deltaLabel,
                        style = AppTheme.typography.priceDelta,
                        color = deltaColor,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
        }
        if (!showDivider) return@Column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(colors.line),
        )
    }
}
