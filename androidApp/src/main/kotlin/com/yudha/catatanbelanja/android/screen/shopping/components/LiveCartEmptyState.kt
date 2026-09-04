package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The cart's own empty copy — no headline, just the trolley and the nudge upwards. */
@Composable
internal fun LiveCartEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🛒", style = AppTheme.typography.emojiLarge)
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = stringResource(R.string.live_cart_empty),
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
