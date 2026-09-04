package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/** Tone of a pill badge. In this app a rising price is bad, so [Up] is coral and [Down] mint. */
enum class AppBadgeTone { Tint, Up, Down, Neutral }

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: AppBadgeTone = AppBadgeTone.Tint,
) {
    val colors = AppTheme.colors
    val background = when (tone) {
        AppBadgeTone.Tint -> colors.tint
        AppBadgeTone.Up -> colors.coralBg
        AppBadgeTone.Down -> colors.mintBg
        AppBadgeTone.Neutral -> colors.background
    }
    val foreground = when (tone) {
        AppBadgeTone.Tint -> colors.primaryDark
        AppBadgeTone.Up -> colors.coral
        AppBadgeTone.Down -> colors.mint
        AppBadgeTone.Neutral -> colors.inkSecondary
    }
    Text(
        text = text,
        modifier = modifier
            .background(background, RoundedCornerShape(AppTheme.shapes.pill))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = AppTheme.typography.fieldLabel,
        color = foreground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
