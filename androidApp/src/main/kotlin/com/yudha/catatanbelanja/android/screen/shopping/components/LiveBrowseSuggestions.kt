package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.domain.model.NameChipView

/** `renderSugg()` with an empty box: what you buy often, then the catalog to browse. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LiveBrowseSuggestions(
    frequentNames: List<NameChipView>,
    selectedCategory: String?,
    categoryItems: List<NameChipView>,
    onPickName: (String) -> Unit,
    onPickCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (frequentNames.isNotEmpty()) {
            Text(
                text = stringResource(R.string.live_suggest_frequent),
                style = AppTheme.typography.fieldLabel,
                color = AppTheme.colors.inkTertiary,
            )
            Spacer(Modifier.height(Spacing.x8))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            ) {
                frequentNames.forEach { chip ->
                    AppChip(
                        text = chip.name,
                        onClick = { onPickName(chip.name) },
                        emoji = chip.emoji,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.x10))
        }

        Text(
            text = stringResource(R.string.live_suggest_category),
            style = AppTheme.typography.fieldLabel,
            color = AppTheme.colors.inkTertiary,
        )
        Spacer(Modifier.height(Spacing.x8))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            CatalogData.categories.forEach { category ->
                AppChip(
                    text = category.name,
                    onClick = { onPickCategory(category.name) },
                    emoji = category.emoji,
                    selected = category.name == selectedCategory,
                    variant = AppChipVariant.Plain,
                )
            }
        }

        if (categoryItems.isEmpty()) return@Column

        Spacer(Modifier.height(Spacing.x10))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            categoryItems.forEach { chip ->
                AppChip(text = chip.name, onClick = { onPickName(chip.name) }, emoji = chip.emoji)
            }
        }
    }
}
