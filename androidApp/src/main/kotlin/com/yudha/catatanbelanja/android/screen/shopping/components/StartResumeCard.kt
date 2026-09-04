package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.domain.model.SessionSummary

/** The `.resume` bar: a dark shortcut back into the session that is already running. */
@Composable
internal fun StartResumeCard(
    summary: SessionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val store = summary.session.store.ifBlank {
        stringResource(R.string.common_session_untitled_store)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.shapes.radius))
            .background(colors.ink)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AppTheme.shapes.radiusSmall))
                .background(colors.paper.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🛒", style = AppTheme.typography.emoji)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_resume_title),
                style = AppTheme.typography.rowTitle,
                color = colors.paper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.home_resume_subtitle,
                    summary.itemCount,
                    store,
                    summary.itemCount,
                    summary.total.toRupiah(),
                ),
                style = AppTheme.typography.tiny,
                color = colors.paper.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(text = "›", style = AppTheme.typography.sectionTitle, color = colors.paper)
    }
}
