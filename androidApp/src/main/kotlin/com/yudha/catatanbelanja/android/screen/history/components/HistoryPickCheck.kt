package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

private const val CHECK_MARK = "✓"

/** The `.check` circle in compare mode — outline until the session is picked, then filled. */
@Composable
internal fun HistoryPickCheck(
    picked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val fill = when (picked) {
        true -> colors.primary
        false -> colors.paper
    }
    val outline = when (picked) {
        true -> colors.primary
        false -> colors.line
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!picked) return@Box

        Text(text = CHECK_MARK, style = AppTheme.typography.label, color = colors.paper)
    }
}
