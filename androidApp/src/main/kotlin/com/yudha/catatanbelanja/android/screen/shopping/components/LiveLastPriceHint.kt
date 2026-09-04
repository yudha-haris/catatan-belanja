package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.domain.model.LastPurchase

/**
 * `renderHint()`: what this item cost last time, and the "Pakai" shortcut that reuses that
 * price, quantity and brand in one tap.
 */
@Composable
internal fun LiveLastPriceHint(
    lastPurchase: LastPurchase,
    onUseLastPrice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val separator = stringResource(R.string.common_separator_dot)
    val priceLine = stringResource(R.string.live_last_price, lastPurchase.price.toRupiah())
    val qtyLine = when (lastPurchase.qty) {
        null -> ""
        else -> stringResource(
            R.string.live_last_price_qty,
            lastPurchase.qty.toQtyLabel(),
            lastPurchase.unit.orEmpty(),
        )
    }
    val bought = listOf(priceLine, qtyLine).filter { it.isNotBlank() }.joinToString(" ")
    val headline = when (lastPurchase.note.isBlank()) {
        true -> bought
        false -> "$bought $separator ${lastPurchase.note}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.shapes.radiusSmall))
            .background(colors.tint)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = headline, style = AppTheme.typography.muted, color = colors.primaryDark)
            Text(
                text = stringResource(
                    R.string.live_last_price_meta,
                    lastPurchase.whenMillis.toDayLabel(),
                    lastPurchase.store,
                ),
                style = AppTheme.typography.tiny,
                color = colors.primaryDark.copy(alpha = 0.75f),
            )
        }

        AppChip(
            text = stringResource(R.string.live_last_price_use),
            onClick = onUseLastPrice,
            selected = true,
        )
    }
}
