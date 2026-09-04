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
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** First run: no headline, just the pitch and a way to fill the app with something to look at. */
@Composable
internal fun StartEmptyState(
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_empty_message),
            style = AppTheme.typography.body,
            color = AppTheme.colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.x12))
        AppButton(
            text = stringResource(R.string.common_try_demo),
            onClick = onSeedDemo,
            variant = AppButtonVariant.Ghost,
            fillWidth = false,
        )
    }
}
