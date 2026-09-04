package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.NameChipView

/** `renderSugg()` while typing: what matches, plus the dark "barang baru" escape hatch. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LiveNameSuggestions(
    query: String,
    suggestions: List<NameChipView>,
    showNewItemChip: Boolean,
    onPickName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typed = query.trim()

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
        verticalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        suggestions.forEach { chip ->
            AppChip(text = chip.name, onClick = { onPickName(chip.name) }, emoji = chip.emoji)
        }

        if (!showNewItemChip) return@FlowRow

        AppChip(
            text = stringResource(R.string.live_new_item_chip, typed),
            onClick = { onPickName(typed) },
            emoji = "＋",
            variant = AppChipVariant.Dark,
        )
    }
}
