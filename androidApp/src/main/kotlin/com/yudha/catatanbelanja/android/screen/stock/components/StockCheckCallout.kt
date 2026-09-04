package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The month-end CTA and the line of small print explaining what it saves. */
@Composable
internal fun StockCheckCallout(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(Spacing.x10))
        AppButton(
            text = stringResource(R.string.stock_check_cta),
            onClick = onClick,
            enabled = enabled,
            emoji = "📋",
            big = true,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = stringResource(R.string.stock_check_cta_hint),
            style = AppTheme.typography.tiny,
            color = AppTheme.colors.inkTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
