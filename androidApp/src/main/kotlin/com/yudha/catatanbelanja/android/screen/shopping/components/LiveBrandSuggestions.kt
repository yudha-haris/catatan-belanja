package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** `renderBrandSugg()`: the brands this item was bought under before. */
@Composable
internal fun LiveBrandSuggestions(
    brands: List<String>,
    onPickBrand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        brands.forEach { brand ->
            AppChip(
                text = brand,
                onClick = { onPickBrand(brand) },
                emoji = "🏷",
                variant = AppChipVariant.Plain,
            )
        }
    }
}
