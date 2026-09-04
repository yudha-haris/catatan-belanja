package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.designsystem.theme.appColorsFor
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

/**
 * One `.theme` card: the flavour's own hero gradient as a swatch, its name, and the 2.5dp
 * selected outline. The unselected outline is drawn in `paper` so picking one never reflows.
 */
@Composable
internal fun SettingsThemeCard(
    flavor: ThemeFlavor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)
    val swatch = appColorsFor(flavor)
    val label = when (flavor) {
        ThemeFlavor.PURPLE -> stringResource(R.string.settings_theme_purple)
        ThemeFlavor.GREEN -> stringResource(R.string.settings_theme_green)
        ThemeFlavor.BLUE -> stringResource(R.string.settings_theme_blue)
    }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .appShadow(shape)
            .clip(shape)
            .background(colors.paper)
            .border(2.5.dp, if (selected) colors.primary else colors.paper, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(swatch.heroStart, swatch.heroEnd))),
        )
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = colors.ink,
            maxLines = 1,
        )
    }
}
