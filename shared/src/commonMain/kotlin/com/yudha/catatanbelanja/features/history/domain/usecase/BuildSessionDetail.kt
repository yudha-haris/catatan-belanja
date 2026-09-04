package com.yudha.catatanbelanja.features.history.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Detail rows for [session], each carrying the price it had in [previous] — the most recent
 * finished session older than this one — matched by normalized name.
 */
class BuildSessionDetail(private val findItemCategory: FindItemCategory) {
    operator fun invoke(
        session: ShoppingSession,
        previous: ShoppingSession?,
    ): List<SessionItemRow> {
        // The prototype's `find`: with the same name bought twice, the first line is the one to beat.
        val previousPrices = previous?.items.orEmpty()
            .groupBy { it.name.normalized() }
            .mapValues { (_, items) -> items.first().price }

        return session.items.map { item ->
            val previousPrice = previousPrices[item.name.normalized()]
            val delta = previousPrice?.let { item.price - it }?.takeIf { it != 0 }
            SessionItemRow(
                item = item,
                emoji = findItemCategory.emojiFor(item.name),
                unitPrice = unitPriceOf(item.price, item.qty),
                previousPrice = previousPrice,
                priceDeltaAmount = delta?.let(::abs),
                isPriceUp = (delta ?: 0) > 0,
            )
        }
    }

    /** A per-unit price only reads as extra information when more than one unit was bought. */
    private fun unitPriceOf(price: Int, qty: Double?): Int? {
        if (qty == null || qty <= 0.0 || qty == 1.0) return null
        return (price / qty).roundToInt()
    }
}
