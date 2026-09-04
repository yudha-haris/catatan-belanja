package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard

/** The four data actions from the prototype, in its order: demo, export, import, wipe. */
@Composable
internal fun SettingsDataCard(
    sessionCount: Int,
    stockCount: Int,
    onSeedDemo: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        SettingsDataRow(
            emoji = "✨",
            title = stringResource(R.string.settings_seed_demo_title),
            subtitle = stringResource(R.string.settings_seed_demo_subtitle),
            onClick = onSeedDemo,
            enabled = enabled,
        )
        SettingsDataRow(
            emoji = "📤",
            title = stringResource(R.string.settings_export_title),
            subtitle = stringResource(R.string.settings_export_subtitle),
            onClick = onExport,
            enabled = enabled,
        )
        SettingsDataRow(
            emoji = "📥",
            title = stringResource(R.string.settings_import_title),
            subtitle = stringResource(R.string.settings_import_subtitle),
            onClick = onImport,
            enabled = enabled,
        )
        SettingsDataRow(
            emoji = "🗑️",
            title = stringResource(R.string.settings_clear_title),
            subtitle = stringResource(
                R.string.settings_clear_subtitle,
                pluralStringResource(
                    R.plurals.settings_clear_subtitle_sessions,
                    sessionCount,
                    sessionCount,
                ),
                pluralStringResource(
                    R.plurals.settings_clear_subtitle_stock,
                    stockCount,
                    stockCount,
                ),
            ),
            onClick = onClearAll,
            enabled = enabled,
            isDanger = true,
            showDivider = false,
        )
    }
}
