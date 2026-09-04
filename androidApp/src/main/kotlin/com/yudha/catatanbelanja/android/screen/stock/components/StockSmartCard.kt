package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRateLabel
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow

/**
 * The estimate, offered directly above the quantity field it would fill.
 *
 * It is a suggestion and is built to read as one: it states the figure, says where the figure came
 * from, and puts a single button next to it. "Pakai" only types the number into the field below —
 * saving is still the user's own tap on Simpan, so the app never quietly overwrites a quantity
 * somebody actually counted.
 */
@Composable
internal fun StockSmartCard(
    shadow: StockShadow,
    enabled: Boolean,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)
    // The card is the thing that just changed when the estimate arrives, so it is the thing that
    // moves — a small settle rather than a fade, which would read as a loading state. Driven by an
    // Animatable because animateFloatAsState would start already at its target and never run.
    val settle = remember { Animatable(0.94f) }
    LaunchedEffect(Unit) {
        settle.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    val confidenceLabel = when (shadow.confidence) {
        RateConfidence.LOW -> stringResource(R.string.stock_smart_confidence_low)
        RateConfidence.MEDIUM -> stringResource(R.string.stock_smart_confidence_medium)
        RateConfidence.HIGH -> stringResource(R.string.stock_smart_confidence_high)
        RateConfidence.EXACT -> stringResource(R.string.stock_smart_confidence_exact)
    }
    val daysLeft = shadow.daysLeft
    val tail = when {
        shadow.isEmpty -> stringResource(R.string.stock_smart_days_left_none)
        daysLeft == null -> stringResource(R.string.stock_smart_days_left_none)
        else -> stringResource(R.string.stock_smart_days_left, daysLeft)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(settle.value)
            .clip(shape)
            .background(colors.tint)
            .border(1.5.dp, colors.primaryLight, shape)
            .padding(Spacing.x14),
    ) {
        Text(
            text = stringResource(R.string.stock_smart_title),
            style = AppTheme.typography.fieldLabel,
            color = colors.primaryDark,
        )
        Spacer(Modifier.height(Spacing.x6))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.common_item_qty_unit,
                    shadow.estimatedQty.toQtyLabel(),
                    shadow.unit,
                ),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sheetTitle,
                color = colors.ink,
                maxLines = 1,
            )
            AppButton(
                text = stringResource(R.string.stock_smart_apply),
                onClick = onApply,
                variant = AppButtonVariant.Soft,
                enabled = enabled,
                fillWidth = false,
            )
        }
        Spacer(Modifier.height(Spacing.x10))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StockConfidenceDots(confidence = shadow.confidence)
            Text(
                text = confidenceLabel,
                style = AppTheme.typography.fieldLabel,
                color = colors.primaryDark,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(Spacing.x4))
        Text(
            text = stringResource(
                R.string.stock_smart_basis,
                shadow.perDayQty.toRateLabel(),
                shadow.unit,
                tail,
            ),
            style = AppTheme.typography.tiny,
            color = colors.inkSecondary,
        )
        Text(
            text = stringResource(R.string.stock_smart_since, shadow.daysSinceUpdate),
            style = AppTheme.typography.tiny,
            color = colors.inkTertiary,
        )
    }
}
