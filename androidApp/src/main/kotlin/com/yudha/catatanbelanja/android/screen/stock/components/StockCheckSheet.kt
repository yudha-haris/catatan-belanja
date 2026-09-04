package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toLongDateLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckRow

/** The prototype caps the scroller at 52dvh so "Simpan cek stok" never leaves the screen. */
private const val LIST_HEIGHT_FRACTION = 0.52f

/**
 * The month-end bulk entry sheet: every stock row pre-filled with its current quantity, edited in
 * one pass and handed back to the ViewModel as raw text keyed by row id.
 */
@Composable
internal fun StockCheckSheet(
    rows: List<StockCheckRow>,
    checkedAtMillis: Long,
    enabled: Boolean,
    onSave: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val quantities = remember(rows) {
        mutableStateMapOf<String, String>().apply {
            rows.forEach { row -> put(row.id, row.previousQty.toQtyLabel()) }
        }
    }
    val listMaxHeight = LocalConfiguration.current.screenHeightDp.dp * LIST_HEIGHT_FRACTION

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.stock_check_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = stringResource(
                R.string.stock_check_sheet_subtitle,
                checkedAtMillis.toLongDateLabel(),
            ),
            style = AppTheme.typography.muted,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x12))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = listMaxHeight),
        ) {
            itemsIndexed(rows, key = { _, row -> row.id }) { index, row ->
                StockCheckInputRow(
                    row = row,
                    value = quantities[row.id].orEmpty(),
                    onValueChange = { quantities[row.id] = it },
                    enabled = enabled,
                    showDivider = index < rows.lastIndex,
                )
            }
        }

        Spacer(Modifier.height(Spacing.x12))
        AppButton(
            text = stringResource(R.string.stock_check_save),
            onClick = { onSave(quantities.toMap()) },
            enabled = enabled,
            big = true,
        )
    }
}
