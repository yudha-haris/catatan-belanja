package com.yudha.catatanbelanja.android.designsystem.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The `.top` row: an optional back pill, the title with an optional muted line above it, trailing actions. */
@Composable
fun AppScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = stringResource(R.string.common_cd_back),
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
        verticalAlignment = Alignment.Top,
    ) {
        if (onBack != null) {
            AppIconButton(
                onClick = onBack,
                contentDescription = backContentDescription,
                icon = Icons.Rounded.ChevronLeft,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTheme.typography.muted,
                    color = AppTheme.colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = title,
                style = AppTheme.typography.screenTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (actions == null) return@Row

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            actions()
        }
    }
}
