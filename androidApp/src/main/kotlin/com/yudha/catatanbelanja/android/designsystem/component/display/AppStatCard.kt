package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

@Composable
fun AppStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    hintTone: AppBadgeTone = AppBadgeTone.Neutral,
) {
    val colors = AppTheme.colors
    val hintColor = when (hintTone) {
        AppBadgeTone.Tint -> colors.primaryDark
        AppBadgeTone.Up -> colors.coral
        AppBadgeTone.Down -> colors.mint
        AppBadgeTone.Neutral -> colors.inkTertiary
    }
    val shape = RoundedCornerShape(AppTheme.shapes.radius)
    Column(
        modifier = modifier
            .appShadow(shape)
            .clip(shape)
            .background(colors.paper)
            .padding(14.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = AppTheme.typography.statValue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (hint == null) return@Column
        Spacer(Modifier.height(2.dp))
        Text(
            text = hint,
            style = AppTheme.typography.tiny,
            color = hintColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
