package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptHeader
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppToggleTile
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toLongDateLabel
import com.yudha.catatanbelanja.android.format.toRupiah

/** `finishSheet()`: the paid receipt, the name it gets filed under, and the stock hand-off. */
@Composable
internal fun LiveFinishSheet(
    total: Int,
    itemCount: Int,
    stockableCount: Int,
    listLeftoverCount: Int,
    defaultName: String,
    finishedAt: Long,
    onConfirm: (name: String, addToStock: Boolean, carryOverList: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(defaultName) { mutableStateOf(defaultName) }
    var addToStock by remember { mutableStateOf(true) }
    var carryOverList by remember { mutableStateOf(true) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            Text(
                text = stringResource(R.string.live_finish_sheet_title),
                style = AppTheme.typography.sheetTitle,
                color = AppTheme.colors.ink,
            )
            Text(text = "🎉", style = AppTheme.typography.sheetTitle)
        }
        Spacer(Modifier.height(Spacing.x16))

        ReceiptHeader(
            label = stringResource(R.string.live_finish_receipt_label),
            amount = total.toRupiah(),
            footerLeft = pluralStringResource(R.plurals.common_item_count, itemCount, itemCount),
            footerRight = finishedAt.toLongDateLabel(),
            compact = true,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.live_finish_name_label),
        )

        if (stockableCount > 0) {
            Spacer(Modifier.height(Spacing.x12))
            AppToggleTile(
                title = "📦 " + stringResource(R.string.live_finish_add_to_stock_title),
                checked = addToStock,
                onCheckedChange = { addToStock = it },
                subtitle = pluralStringResource(
                    R.plurals.live_finish_add_to_stock_subtitle,
                    stockableCount,
                    stockableCount,
                ),
            )
        }

        if (listLeftoverCount > 0) {
            Spacer(Modifier.height(Spacing.x12))
            AppToggleTile(
                title = "📝 " + stringResource(R.string.live_finish_carry_over_title),
                checked = carryOverList,
                onCheckedChange = { carryOverList = it },
                subtitle = pluralStringResource(
                    R.plurals.live_finish_carry_over_subtitle,
                    listLeftoverCount,
                    listLeftoverCount,
                ),
            )
        }

        Spacer(Modifier.height(Spacing.x16))
        AppButton(
            text = stringResource(R.string.live_finish_confirm),
            // `confirmFinish()` falls back to the seeded default when the field is cleared.
            onClick = {
                onConfirm(
                    name.ifBlank { defaultName },
                    addToStock && stockableCount > 0,
                    carryOverList && listLeftoverCount > 0,
                )
            },
            big = true,
        )
        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = stringResource(R.string.live_finish_continue),
            onClick = onDismiss,
            variant = AppButtonVariant.Soft,
        )
    }
}
