package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The foot of a summary card: the one chip that opens the whole page behind it. Right-aligned and
 * quiet on purpose — the card's own content is what the tab is for; this is the way out of it.
 */
@Composable
internal fun DashboardSeeAllRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(Modifier.height(Spacing.x10))
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        AppChip(text = text, onClick = onClick, variant = AppChipVariant.Plain)
    }
}
