package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The prototype's storage banner. SQLite is unconditional here, so the "memory only" warning
 * branch is gone — this always reports the green, on-device case.
 */
@Composable
internal fun SettingsStorageCard(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier, flat = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.mintBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✅", style = AppTheme.typography.emoji)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_storage_title),
                    style = AppTheme.typography.rowTitle,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(R.string.settings_storage_message),
                    style = AppTheme.typography.tiny,
                    color = colors.inkTertiary,
                )
            }
        }
    }
}
