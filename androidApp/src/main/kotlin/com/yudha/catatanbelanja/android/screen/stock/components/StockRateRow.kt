package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRateLabel
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow

/**
 * One quiet line in the editor, and the only thing smart stock adds to a sheet the user opens for
 * ordinary reasons. Everything it can do lives behind it in [StockRateSheet] — the alternative was
 * three modes, a quantity, a unit and a period all sitting in the middle of the edit form, which
 * is how a two-field sheet turns into something people close again.
 */
@Composable
internal fun StockRateRow(
    mode: RateMode,
    shadow: StockShadow?,
    autoEstimate: StockRateEstimate?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)
    val interactionSource = remember { MutableInteractionSource() }
    // The pace actually in force: what the user set, else what the history says, else nothing yet.
    val perDay = when (mode) {
        RateMode.MANUAL -> shadow?.perDayQty
        RateMode.AUTO -> autoEstimate?.perDayQty
        RateMode.OFF -> null
    }
    val unit = shadow?.unit ?: autoEstimate?.unit.orEmpty()
    val paceLabel = when {
        perDay == null -> null
        else -> stringResource(R.string.stock_rate_per_day, perDay.toRateLabel(), unit)
    }
    val value = when (mode) {
        RateMode.OFF -> stringResource(R.string.stock_rate_value_off)
        // The pace is stated but currently produces no estimate — an empty shelf, or a quantity
        // written down today. Say the mode without trailing a separator into nothing.
        RateMode.MANUAL -> when (paceLabel) {
            null -> stringResource(R.string.stock_rate_value_manual_plain)
            else -> stringResource(R.string.stock_rate_value_manual, paceLabel)
        }
        RateMode.AUTO -> when (paceLabel) {
            null -> stringResource(R.string.stock_rate_value_auto_learning)
            else -> stringResource(R.string.stock_rate_value_auto, paceLabel)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.background)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.x14, vertical = Spacing.x12),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.stock_rate_row_label),
                style = AppTheme.typography.fieldLabel,
                color = colors.inkSecondary,
            )
            Text(
                text = value,
                style = AppTheme.typography.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.inkTertiary,
        )
    }
}
