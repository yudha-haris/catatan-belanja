package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard

/** One row into the preset hub — the four lists live a screen deeper, not in Pengaturan itself. */
@Composable
internal fun SettingsPresetCard(
    onOpenPreset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        SettingsDataRow(
            emoji = "🧾",
            title = stringResource(R.string.settings_preset_row_title),
            subtitle = stringResource(R.string.settings_preset_row_subtitle),
            onClick = onOpenPreset,
            showDivider = false,
        )
    }
}
