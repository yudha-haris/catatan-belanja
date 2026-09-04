package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRateLabel
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.RatePeriod
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate

/**
 * The whole of smart stock's configuration, in the one place the user has to ask for it.
 *
 * Three modes, and the manual fields only exist while manual is chosen — an inert quantity box
 * sitting under an "Automatic" chip invites the user to fill it in and then wonder why nothing
 * happened. Automatic shows what the history already concluded, so choosing it is an informed
 * choice rather than a leap of faith.
 */
@Composable
internal fun StockRateSheet(
    itemName: String,
    emoji: String,
    mode: RateMode,
    manualQty: String,
    manualUnit: String,
    manualPeriod: RatePeriod,
    units: List<String>,
    autoEstimate: StockRateEstimate?,
    enabled: Boolean,
    onSave: (mode: RateMode, qtyText: String, unit: String, period: RatePeriod) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedMode by remember(mode) { mutableStateOf(mode) }
    var qtyText by remember(manualQty) { mutableStateOf(manualQty) }
    var selectedUnit by remember(manualUnit) { mutableStateOf(manualUnit) }
    var selectedPeriod by remember(manualPeriod) { mutableStateOf(manualPeriod) }

    val hint = when (selectedMode) {
        RateMode.AUTO -> stringResource(R.string.stock_rate_hint_auto)
        RateMode.MANUAL -> stringResource(R.string.stock_rate_hint_manual)
        RateMode.OFF -> stringResource(R.string.stock_rate_hint_off)
    }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.stock_rate_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Text(
            text = "$emoji $itemName",
            style = AppTheme.typography.muted,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x16))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            AppChip(
                text = stringResource(R.string.stock_rate_mode_auto),
                onClick = { selectedMode = RateMode.AUTO },
                emoji = "✨",
                selected = selectedMode == RateMode.AUTO,
                enabled = enabled,
            )
            AppChip(
                text = stringResource(R.string.stock_rate_mode_manual),
                onClick = { selectedMode = RateMode.MANUAL },
                emoji = "✍️",
                selected = selectedMode == RateMode.MANUAL,
                enabled = enabled,
            )
            AppChip(
                text = stringResource(R.string.stock_rate_mode_off),
                onClick = { selectedMode = RateMode.OFF },
                selected = selectedMode == RateMode.OFF,
                enabled = enabled,
            )
        }

        AnimatedVisibility(
            visible = selectedMode == RateMode.MANUAL,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(Spacing.x16))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x10)) {
                    AppTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.stock_rate_qty_label),
                        placeholder = stringResource(R.string.stock_rate_qty_placeholder),
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    AppUnitDropdown(
                        value = selectedUnit,
                        onValueChange = { selectedUnit = it },
                        modifier = Modifier.weight(1f),
                        units = units,
                        label = stringResource(R.string.common_unit_label),
                        enabled = enabled,
                    )
                }
                Spacer(Modifier.height(Spacing.x12))
                Text(
                    text = stringResource(R.string.stock_rate_period_label),
                    style = AppTheme.typography.fieldLabel,
                    color = AppTheme.colors.inkSecondary,
                )
                Spacer(Modifier.height(Spacing.x8))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x8)) {
                    AppChip(
                        text = stringResource(R.string.stock_rate_period_day),
                        onClick = { selectedPeriod = RatePeriod.DAY },
                        selected = selectedPeriod == RatePeriod.DAY,
                        enabled = enabled,
                    )
                    AppChip(
                        text = stringResource(R.string.stock_rate_period_week),
                        onClick = { selectedPeriod = RatePeriod.WEEK },
                        selected = selectedPeriod == RatePeriod.WEEK,
                        enabled = enabled,
                    )
                    AppChip(
                        text = stringResource(R.string.stock_rate_period_month),
                        onClick = { selectedPeriod = RatePeriod.MONTH },
                        selected = selectedPeriod == RatePeriod.MONTH,
                        enabled = enabled,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.x14))
        Text(
            text = hint,
            style = AppTheme.typography.tiny,
            color = AppTheme.colors.inkTertiary,
        )

        if (selectedMode == RateMode.AUTO) {
            Spacer(Modifier.height(Spacing.x6))
            Text(
                text = when (autoEstimate) {
                    null -> stringResource(R.string.stock_rate_auto_none)
                    else -> stringResource(
                        R.string.stock_rate_auto_found,
                        autoEstimate.perDayQty.toRateLabel(),
                        autoEstimate.unit,
                        autoEstimate.windowCount,
                    )
                },
                style = AppTheme.typography.tiny,
                color = AppTheme.colors.inkSecondary,
            )
        }

        Spacer(Modifier.height(Spacing.x20))
        AppButton(
            text = stringResource(R.string.common_save),
            onClick = { onSave(selectedMode, qtyText, selectedUnit, selectedPeriod) },
            enabled = enabled,
        )
    }
}
