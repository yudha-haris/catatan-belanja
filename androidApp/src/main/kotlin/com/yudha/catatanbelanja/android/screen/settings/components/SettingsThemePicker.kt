package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

/** The `.themes` grid: one equal-width card per flavour, in declaration order. */
@Composable
internal fun SettingsThemePicker(
    selected: ThemeFlavor,
    onSelect: (ThemeFlavor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
    ) {
        ThemeFlavor.entries.forEach { flavor ->
            SettingsThemeCard(
                flavor = flavor,
                selected = flavor == selected,
                onClick = { onSelect(flavor) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
