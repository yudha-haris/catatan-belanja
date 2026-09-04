package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockUsageRow

/** The `.cmp-row` of the log sheet: name (1.4fr), "beli" (1fr), "sisa" with the inferred usage (1fr). */
@Composable
internal fun StockUsageLine(
    row: StockUsageRow,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val bought = when {
        row.hasBought -> stringResource(R.string.common_item_qty_unit, row.boughtQty.toQtyLabel(), row.unit)
        else -> stringResource(R.string.common_empty_value)
    }
    val remaining = when {
        row.isOut -> stringResource(R.string.stock_out)
        else -> stringResource(R.string.common_item_qty_unit, row.remainingQty.toQtyLabel(), row.unit)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.x10),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${row.emoji} ${row.name}",
                modifier = Modifier.weight(1.4f),
                style = AppTheme.typography.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.stock_log_bought),
                    style = AppTheme.typography.tiny,
                    color = colors.inkTertiary,
                )
                Text(
                    text = bought,
                    style = AppTheme.typography.price,
                    color = colors.ink,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.stock_log_remaining),
                    style = AppTheme.typography.tiny,
                    color = colors.inkTertiary,
                )
                Text(
                    text = remaining,
                    style = AppTheme.typography.price,
                    color = if (row.isOut) colors.coral else colors.ink,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                if (row.usedQty != null) {
                    Text(
                        text = stringResource(R.string.stock_log_used, row.usedQty.toQtyLabel()),
                        style = AppTheme.typography.tiny,
                        color = colors.inkTertiary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
        }

        if (!showDivider) return@Column

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(colors.line),
        )
    }
}
