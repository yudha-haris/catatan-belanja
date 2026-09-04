package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private val TipKeys = listOf(
    R.string.settings_tip_pick_frequent,
    R.string.settings_tip_qty_optional,
    R.string.settings_tip_note_later,
    R.string.settings_tip_compare,
    R.string.settings_tip_stock_check,
)

/** The `Tips cepat` card — the prototype's five bullets, same order, same copy. */
@Composable
internal fun SettingsTipsCard(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier, flat = true) {
        Text(
            text = stringResource(R.string.settings_tips_title),
            style = AppTheme.typography.rowTitle,
            color = colors.ink,
        )
        Spacer(Modifier.height(Spacing.x6))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.x4)) {
            TipKeys.forEach { key ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // A bullet glyph, not copy — same category as the emoji tiles.
                    Text(text = "•", style = AppTheme.typography.muted, color = colors.inkSecondary)
                    Text(
                        text = stringResource(key),
                        style = AppTheme.typography.muted,
                        color = colors.inkSecondary,
                    )
                }
            }
        }
    }
}
