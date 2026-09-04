package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptHeader
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.history.presentation.SessionDetailState

private const val ARROW_UP = "▲"
private const val ARROW_DOWN = "▼"

/** The compact receipt; its right footer carries the swing against the previous session. */
@Composable
internal fun SessionDetailReceipt(
    summary: SessionSummary,
    state: SessionDetailState,
    modifier: Modifier = Modifier,
) {
    val arrow = when (state.isTotalUp) {
        true -> ARROW_UP
        false -> ARROW_DOWN
    }
    val deltaLabel = "$arrow ${state.totalDeltaAmount.toRupiah()}"

    ReceiptHeader(
        label = stringResource(R.string.detail_receipt_label),
        amount = summary.total.toRupiah(),
        footerLeft = pluralStringResource(
            R.plurals.common_item_count,
            summary.itemCount,
            summary.itemCount,
        ),
        footerRight = when (state.hasPrevious) {
            true -> stringResource(R.string.detail_vs_previous, deltaLabel)
            false -> ""
        },
        modifier = modifier,
        compact = true,
    )
}
