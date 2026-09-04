package com.yudha.catatanbelanja.android.screen.preset.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** Says what the language setting does not touch: the catalog and everything already typed. */
@Composable
internal fun PresetLanguageNote(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier, flat = true) {
        Text(
            text = stringResource(R.string.preset_language_note_title),
            style = AppTheme.typography.rowTitle,
            color = colors.ink,
        )
        Spacer(Modifier.height(Spacing.x6))
        Text(
            text = stringResource(R.string.preset_language_note_message),
            style = AppTheme.typography.muted,
            color = colors.inkSecondary,
        )
    }
}
