package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPurchase

/**
 * The manual correction, one purchase at a time.
 *
 * It says plainly that the receipt is not being edited, because it isn't: the trip stays exactly
 * as it was logged in the shop, and this number is read by the trend and by nothing else.
 */
@Composable
internal fun TrendQtySheet(
    purchase: TrendPurchase,
    unitOptions: List<String>,
    enabled: Boolean,
    onSave: (qtyText: String, unit: String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var qtyText by remember(purchase.itemId) {
        mutableStateOf(purchase.effectiveQty.toQtyLabel())
    }
    var unit by remember(purchase.itemId) {
        mutableStateOf(purchase.effectiveUnit ?: unitOptions.firstOrNull().orEmpty())
    }
    val qtyFocus = remember { FocusRequester() }
    // Locals so they narrow: the model lives in :shared, which blocks the smart cast.
    val recordedQty = purchase.recordedQty
    val recordedUnit = purchase.recordedUnit

    // The sheet exists to take a number; nothing else on it is worth a tap first.
    LaunchedEffect(purchase.itemId) { qtyFocus.requestFocus() }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.trend_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = "${purchase.endedAt.toDayLabel()} · ${purchase.price.toRupiah()}",
            style = AppTheme.typography.muted,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x6))
        Text(
            text = when {
                recordedQty == null || recordedUnit == null ->
                    stringResource(R.string.trend_sheet_recorded_none)
                else -> stringResource(
                    R.string.trend_sheet_recorded,
                    "${recordedQty.toQtyLabel()} $recordedUnit",
                )
            },
            style = AppTheme.typography.tiny,
        )
        Spacer(Modifier.height(Spacing.x16))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        ) {
            AppTextField(
                value = qtyText,
                onValueChange = { qtyText = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(qtyFocus),
                label = stringResource(R.string.trend_sheet_qty_label),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AppUnitDropdown(
                value = unit,
                onValueChange = { unit = it },
                modifier = Modifier.weight(1f),
                units = unitOptions,
                label = stringResource(R.string.trend_base_unit_label),
                enabled = enabled,
            )
        }
        Spacer(Modifier.height(Spacing.x12))
        Text(
            text = stringResource(R.string.trend_sheet_message),
            style = AppTheme.typography.tiny,
        )
        Spacer(Modifier.height(Spacing.x18))

        AppButton(
            text = stringResource(R.string.common_save),
            onClick = { onSave(qtyText, unit) },
            enabled = enabled,
        )

        if (!purchase.isOverridden) return@AppBottomSheet

        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = stringResource(R.string.trend_sheet_reset),
            onClick = onClear,
            variant = AppButtonVariant.Ghost,
            enabled = enabled,
        )
    }
}
