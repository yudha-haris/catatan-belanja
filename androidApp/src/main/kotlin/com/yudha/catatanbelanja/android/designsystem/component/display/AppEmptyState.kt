package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

@Composable
fun AppEmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, style = AppTheme.typography.emojiLarge)
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = title,
            style = AppTheme.typography.sectionTitle,
            color = AppTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.x6))
        Text(
            text = message,
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
        if (action == null) return@Column
        Spacer(Modifier.height(Spacing.x14))
        action()
    }
}
