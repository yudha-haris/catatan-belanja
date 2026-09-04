package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** A ranked spending row: numbered badge, title + value, share bar and a hint line. */
@Composable
fun AppRankRow(
    rank: Int,
    emoji: String,
    title: String,
    valueLabel: String,
    ratio: Float,
    hint: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.tint),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rank.toString(),
                    style = AppTheme.typography.fieldLabel,
                    color = colors.primaryDark,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$emoji $title",
                        modifier = Modifier.weight(1f, fill = false),
                        style = AppTheme.typography.rowTitle,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.size(Spacing.x8))
                    Text(
                        text = valueLabel,
                        style = AppTheme.typography.rowTitle,
                        color = colors.ink,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(5.dp))
                AppLevelBar(progress = ratio)
                Spacer(Modifier.height(Spacing.x4))
                Text(
                    text = hint,
                    style = AppTheme.typography.tiny,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
