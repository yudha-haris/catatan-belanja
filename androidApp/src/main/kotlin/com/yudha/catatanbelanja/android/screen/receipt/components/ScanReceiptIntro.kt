package com.yudha.catatanbelanja.android.screen.receipt.components

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
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val SCAN_EMOJI = "🧾"
private const val LOCKED_EMOJI = "🔌"

/**
 * What the screen shows before a photo has been read. [available] is false when the build carries
 * no OpenRouter key, and then there is no button — an entry point that can only fail is worse than
 * a sentence explaining why it is not there.
 */
@Composable
internal fun ScanReceiptIntro(
    available: Boolean,
    onChoosePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!available) {
        AppEmptyState(
            emoji = LOCKED_EMOJI,
            title = stringResource(R.string.scan_unavailable_title),
            message = stringResource(R.string.scan_unavailable_message),
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppEmptyState(
            emoji = SCAN_EMOJI,
            title = stringResource(R.string.scan_intro_title),
            message = stringResource(R.string.scan_intro_message),
            action = {
                AppButton(
                    text = stringResource(R.string.scan_intro_cta),
                    onClick = onChoosePhoto,
                    big = true,
                    fillWidth = false,
                )
            },
        )
        Spacer(Modifier.height(Spacing.x16))

        // Said plainly and up front: this is the one screen in an offline app that uses the
        // network, and the user is entitled to know before they photograph anything.
        Text(
            text = stringResource(R.string.scan_intro_hint),
            style = AppTheme.typography.tiny,
            color = AppTheme.colors.inkTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.x24),
        )
    }
}
