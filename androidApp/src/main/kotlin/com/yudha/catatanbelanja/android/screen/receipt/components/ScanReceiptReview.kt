package com.yudha.catatanbelanja.android.screen.receipt.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.receipt.domain.model.ScannedItemRow
import com.yudha.catatanbelanja.features.receipt.presentation.ScanReceiptState

private const val RETAKE_EMOJI = "🔄"

/**
 * The draft, laid out for checking. The three header fields are Compose-local buffers owned by the
 * screen — they are read once, on save — so typing in them never round-trips through the view
 * model and never fights a reseed.
 */
@Composable
internal fun ScanReceiptReview(
    state: ScanReceiptState,
    tripName: String,
    onTripNameChange: (String) -> Unit,
    store: String,
    onStoreChange: (String) -> Unit,
    dateText: String,
    onDateChange: (String) -> Unit,
    onItemClicked: (ScannedItemRow) -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppCard {
            Text(
                text = stringResource(R.string.scan_review_title),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
            )
            Spacer(Modifier.height(Spacing.x6))
            Text(
                text = stringResource(R.string.scan_review_message),
                style = AppTheme.typography.muted,
                color = AppTheme.colors.inkSecondary,
            )
            Spacer(Modifier.height(Spacing.x16))

            AppTextField(
                value = tripName,
                onValueChange = onTripNameChange,
                label = stringResource(R.string.scan_trip_name_label),
                placeholder = stringResource(R.string.scan_trip_name_placeholder),
            )
            Spacer(Modifier.height(Spacing.x12))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
                AppTextField(
                    value = store,
                    onValueChange = onStoreChange,
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.scan_store_label),
                    placeholder = stringResource(R.string.scan_store_placeholder),
                )
                AppTextField(
                    value = dateText,
                    onValueChange = onDateChange,
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.scan_date_label),
                    placeholder = stringResource(R.string.scan_date_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            if (!state.dateWasRead) {
                Spacer(Modifier.height(Spacing.x8))
                Text(
                    text = stringResource(R.string.scan_date_guessed),
                    style = AppTheme.typography.tiny,
                    color = AppTheme.colors.inkTertiary,
                )
            }
        }
        Spacer(Modifier.height(Spacing.x18))

        AppSectionHeader(
            title = stringResource(R.string.scan_items_title),
            trailing = {
                AppButton(
                    text = stringResource(R.string.scan_retake),
                    onClick = onRetake,
                    variant = AppButtonVariant.Ghost,
                    emoji = RETAKE_EMOJI,
                    fillWidth = false,
                )
            },
        )

        state.rows.forEach { row ->
            ScanReceiptItemRow(
                row = row,
                onClick = { onItemClicked(row) },
            )
        }
    }
}
