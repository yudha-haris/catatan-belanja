package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The `.home-hero` block: the store name, the recent-store chips and the big "Mulai belanja". */
@Composable
internal fun StartHeroCard(
    hasActiveSession: Boolean,
    store: String,
    onStoreChange: (String) -> Unit,
    storeSuggestions: List<String>,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSheet)
    val label = when (hasActiveSession) {
        true -> stringResource(R.string.home_hero_label_again)
        false -> stringResource(R.string.home_hero_label_new)
    }
    val title = when (hasActiveSession) {
        true -> stringResource(R.string.home_hero_title_again)
        false -> stringResource(R.string.home_hero_title_new)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                clip = false,
                ambientColor = colors.primary,
                spotColor = colors.primary,
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.heroStart, colors.heroEnd),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 18.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.muted,
            color = colors.paper.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(Spacing.x4))
        Text(text = title, style = AppTheme.typography.heroTitle, color = colors.paper)
        Spacer(Modifier.height(Spacing.x14))

        StartStoreField(value = store, onValueChange = onStoreChange)

        if (storeSuggestions.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.x10))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            ) {
                storeSuggestions.forEach { suggestion ->
                    AppChip(
                        text = suggestion,
                        onClick = { onStoreChange(suggestion) },
                        variant = AppChipVariant.OnHero,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.x14))
        AppButton(
            text = stringResource(R.string.home_start_button),
            onClick = onStartSession,
            variant = AppButtonVariant.OnHero,
            emoji = "🛒",
            big = true,
        )
    }
}
