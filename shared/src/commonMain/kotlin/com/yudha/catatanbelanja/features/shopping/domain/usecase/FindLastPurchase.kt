package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.LastPurchase
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

/** The prototype's `lastPrice(name)`: newest finished session that ever held this name. */
class FindLastPurchase {
    operator fun invoke(name: String, sessions: List<ShoppingSession>): LastPurchase? {
        val key = name.normalized()
        if (key.isEmpty()) return null

        sessions.sortedByDescending { it.endedAt ?: it.startedAt }.forEach { session ->
            val item = session.items.firstOrNull { it.name.normalized() == key } ?: return@forEach
            return LastPurchase(
                price = item.price,
                qty = item.qty,
                unit = item.unit,
                note = item.note,
                whenMillis = session.endedAt ?: session.startedAt,
                // The hint reads "· Superindo": the prototype shows the session name here.
                store = session.name,
            )
        }
        return null
    }
}
