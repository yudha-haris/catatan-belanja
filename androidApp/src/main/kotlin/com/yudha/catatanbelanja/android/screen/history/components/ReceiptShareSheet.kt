package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.capture.AppCaptureBox
import com.yudha.catatanbelanja.android.capture.rememberAppCaptureController
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptPaperLine
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toLongDateLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toTimeLabel
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow
import kotlinx.coroutines.launch

private const val NOTE_EMOJI = "🏷"
private const val SHARE_EMOJI = "📤"

/**
 * The receipt, previewed exactly as it will be shared, with one button under it.
 *
 * Preview and payload are the same composable inside a capture box, so there is no second
 * rendering path that could drift from what the user was shown. It is also why the sheet scrolls
 * rather than lazily building its rows: the layer records the node whole, however far past the
 * bottom of the screen the receipt runs, but only if every row was actually composed.
 */
@Composable
internal fun ReceiptShareSheet(
    summary: SessionSummary,
    rows: List<SessionItemRow>,
    onShare: (ByteArray) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = rememberAppCaptureController()
    val scope = rememberCoroutineScope()
    val session = summary.session
    val endedAt = session.endedAt ?: session.startedAt
    val separator = " ${stringResource(R.string.common_separator_dot)} "

    val lines = rows.map { row ->
        val item = row.item
        val qtyPart = item.qty?.let { qty ->
            stringResource(R.string.common_item_qty_unit, qty.toQtyLabel(), item.unit.orEmpty())
        }
        val notePart = item.note.takeIf { it.isNotBlank() }?.let { "$NOTE_EMOJI $it" }
        ReceiptPaperLine(
            emoji = row.emoji,
            name = item.name,
            detail = listOfNotNull(qtyPart, notePart).joinToString(separator),
            amount = item.price.toRupiah(),
        )
    }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.receipt_share_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x6))
        Text(
            text = stringResource(R.string.receipt_share_sheet_hint),
            style = AppTheme.typography.body,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppCaptureBox(controller = controller) {
            ReceiptShareCanvas(
                // A trip can be filed with neither a name nor a store; the receipt still needs
                // a heading, and an empty one would print as a blank strip of paper.
                storeName = session.name
                    .ifBlank { session.store }
                    .ifBlank { stringResource(R.string.common_session_untitled_store) },
                dateLabel = stringResource(
                    R.string.receipt_share_date,
                    endedAt.toLongDateLabel(),
                    endedAt.toTimeLabel(),
                ),
                lines = lines,
                itemCountLabel = pluralStringResource(
                    R.plurals.common_item_count,
                    summary.itemCount,
                    summary.itemCount,
                ),
                totalAmount = summary.total.toRupiah(),
                serialLabel = session.id,
            )
        }

        Spacer(Modifier.height(Spacing.x16))
        AppButton(
            text = stringResource(R.string.receipt_share_confirm),
            onClick = {
                scope.launch { controller.capturePng()?.let(onShare) }
            },
            emoji = SHARE_EMOJI,
            big = true,
        )
        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = stringResource(R.string.common_close),
            onClick = onDismiss,
            variant = AppButtonVariant.Soft,
        )
    }
}
