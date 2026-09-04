package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptPaper
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptPaperLine
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * Exactly what gets captured and shared: the paper receipt lying on the flavour's hero gradient.
 *
 * The margin is not decoration. A shared image lands in a chat as a rectangle of its own, and a
 * white receipt on a white chat background has no edges — the torn ends, which are the whole
 * conceit, would be invisible.
 */
@Composable
internal fun ReceiptShareCanvas(
    storeName: String,
    dateLabel: String,
    lines: List<ReceiptPaperLine>,
    itemCountLabel: String,
    totalAmount: String,
    serialLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.heroStart, colors.heroEnd),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .padding(CANVAS_MARGIN),
    ) {
        ReceiptPaper(
            brandLabel = stringResource(R.string.receipt_share_brand),
            storeName = storeName,
            dateLabel = dateLabel,
            itemsHeaderLabel = stringResource(R.string.receipt_share_items_header),
            amountHeaderLabel = stringResource(R.string.receipt_share_amount_header),
            lines = lines,
            itemCountLabel = itemCountLabel,
            totalLabel = stringResource(R.string.receipt_share_total),
            totalAmount = totalAmount,
            stampLabel = stringResource(R.string.receipt_share_stamp),
            serialLabel = serialLabel,
            footNote = stringResource(R.string.receipt_share_footer),
        )
    }
}

private val CANVAS_MARGIN = 18.dp
