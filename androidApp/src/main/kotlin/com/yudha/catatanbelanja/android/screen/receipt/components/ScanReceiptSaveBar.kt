package com.yudha.catatanbelanja.android.screen.receipt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.format.toRupiah

/** The pinned `.finish` bar: what the scan adds up to, and the one button that writes it. */
@Composable
internal fun ScanReceiptSaveBar(
    total: Int,
    itemCount: Int,
    canSave: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.shapes.radius))
            .background(colors.ink)
            .padding(start = 18.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.shapes.radiusSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(R.plurals.scan_review_count, itemCount, itemCount),
                style = AppTheme.typography.tiny,
                color = colors.paper.copy(alpha = 0.7f),
            )
            Text(
                text = total.toRupiah(),
                style = AppTheme.typography.sectionTitle,
                color = colors.paper,
            )
        }

        AppButton(
            text = stringResource(R.string.scan_save),
            onClick = onSave,
            enabled = canSave,
            fillWidth = false,
        )
    }
}
