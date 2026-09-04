package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahSigned
import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPurchase

/**
 * One purchase, and whether the trend could actually use it. A row it could not is the point of
 * the list — it says "needs a quantity" in coral and opens the sheet that fixes it.
 */
@Composable
internal fun TrendPurchaseRow(
    purchase: TrendPurchase,
    basis: PriceBasis,
    baseUnit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pulled into locals so they narrow: the model lives in :shared, and Kotlin will not smart
    // cast a public property from another module.
    val effectiveQty = purchase.effectiveQty
    val effectiveUnit = purchase.effectiveUnit
    val qty = when {
        effectiveQty == null || effectiveUnit == null -> stringResource(R.string.trend_row_no_qty)
        else -> stringResource(R.string.trend_row_qty, effectiveQty.toQtyLabel(), effectiveUnit)
    }
    val note = when {
        purchase.isOverridden -> stringResource(R.string.trend_row_adjusted)
        !purchase.isUsable -> stringResource(R.string.trend_row_needs_qty)
        else -> null
    }
    val subtitle = when (note) {
        null -> "${purchase.endedAt.toDayLabel()} · $qty"
        else -> "${purchase.endedAt.toDayLabel()} · $qty · $note"
    }
    // In per-unit mode the trailing figure is the derived price, so the receipt total moves to the
    // second line — otherwise the row shows a number that is on no receipt anywhere and says why.
    val trailing = when (basis) {
        PriceBasis.RAW -> purchase.price.toRupiah()
        PriceBasis.PER_UNIT -> when (purchase.isUsable) {
            true -> stringResource(R.string.trend_row_unit_price, purchase.value.toRupiah(), baseUnit)
            false -> purchase.price.toRupiah()
        }
    }
    val tone = when {
        purchase.isUp -> AppBadgeTone.Up
        purchase.isDown -> AppBadgeTone.Down
        else -> AppBadgeTone.Neutral
    }

    AppListRow(
        title = when (purchase.store.isBlank()) {
            true -> stringResource(R.string.report_trip_untitled)
            false -> purchase.store
        },
        modifier = modifier,
        subtitle = subtitle,
        trailing = trailing,
        trailingSub = if (purchase.hasDelta) purchase.deltaAmount.toRupiahSigned() else null,
        trailingSubTone = tone,
        emoji = if (purchase.isUsable) "🧾" else "❓",
        onClick = onClick,
    )
}
