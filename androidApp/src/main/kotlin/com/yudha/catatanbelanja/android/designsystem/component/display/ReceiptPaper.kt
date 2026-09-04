package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** One printed line of [ReceiptPaper]. The caller composes [detail] — quantity, unit, brand. */
data class ReceiptPaperLine(
    val emoji: String,
    val name: String,
    val detail: String,
    val amount: String,
)

/**
 * A shopping trip printed as a till roll: torn at both ends, dashed rules between the sections, a
 * stamp over the total and a barcode nobody will ever scan.
 *
 * Every string arrives finished. The component decides what a receipt looks like and nothing else —
 * what a line says, and in which language, stays with the screen that assembles it.
 */
@Composable
fun ReceiptPaper(
    brandLabel: String,
    storeName: String,
    dateLabel: String,
    itemsHeaderLabel: String,
    amountHeaderLabel: String,
    lines: List<ReceiptPaperLine>,
    itemCountLabel: String,
    totalLabel: String,
    totalAmount: String,
    stampLabel: String,
    serialLabel: String,
    footNote: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        AppTornEdge(color = colors.paper, pointingDown = false)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.paper)
                .padding(horizontal = PAPER_PADDING, vertical = Spacing.x8),
        ) {
            Text(
                text = brandLabel,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.receiptBrand,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(Modifier.height(Spacing.x6))
            Text(
                text = storeName,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.screenTitle,
                color = colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Spacing.x4))
            Text(
                text = dateLabel,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )

            Spacer(Modifier.height(Spacing.x16))
            AppDashedRule()
            Spacer(Modifier.height(Spacing.x8))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = itemsHeaderLabel,
                    modifier = Modifier.weight(1f),
                    style = AppTheme.typography.receiptBrand,
                    maxLines = 1,
                )
                Text(
                    text = amountHeaderLabel,
                    style = AppTheme.typography.receiptBrand,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(Spacing.x8))
            AppDashedRule()
            Spacer(Modifier.height(Spacing.x12))

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.x12)) {
                lines.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
                    ) {
                        Text(text = line.emoji, style = AppTheme.typography.emoji)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = line.name,
                                style = AppTheme.typography.rowTitle,
                                color = colors.ink,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (line.detail.isNotBlank()) {
                                Text(
                                    text = line.detail,
                                    style = AppTheme.typography.muted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Text(
                            text = line.amount,
                            style = AppTheme.typography.price,
                            color = colors.ink,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.x14))
            AppDashedRule()
            Spacer(Modifier.height(Spacing.x14))

            // The stamp sits opposite the total rather than over it: a rubber stamp across a
            // four-digit figure is a joke the first time and an unreadable receipt every time after.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppStampBadge(text = stampLabel)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = totalLabel,
                        style = AppTheme.typography.receiptBrand,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(Spacing.x4))
                    Text(
                        text = totalAmount,
                        style = AppTheme.typography.receiptTotalSmall,
                        color = colors.primary,
                        maxLines = 1,
                    )
                    Text(
                        text = itemCountLabel,
                        style = AppTheme.typography.muted,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.x16))
            AppDashedRule()
            Spacer(Modifier.height(Spacing.x16))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BARCODE_INSET),
            ) {
                AppBarcode(seed = serialLabel)
            }
            Spacer(Modifier.height(Spacing.x6))
            Text(
                text = serialLabel,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.tiny,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(Modifier.height(Spacing.x14))
            Text(
                text = footNote,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(Spacing.x8))
        }

        AppTornEdge(color = colors.paper, pointingDown = true)
    }
}

private val PAPER_PADDING = 22.dp
private val BARCODE_INSET = 18.dp
