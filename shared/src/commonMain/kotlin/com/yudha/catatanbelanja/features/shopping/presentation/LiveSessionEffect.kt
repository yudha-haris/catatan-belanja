package com.yudha.catatanbelanja.features.shopping.presentation

import com.yudha.catatanbelanja.core.common.Failure

sealed interface LiveSessionEffect {
    /** The screen toasts "✓ Beras Bimoli Rp72.000" and clears its input buffers. */
    data class ItemAdded(val name: String, val note: String, val price: Int) : LiveSessionEffect

    data class Finished(
        val sessionId: String,
        val addedToStock: Int,
        val carriedOverToList: Int,
    ) : LiveSessionEffect

    data object Cancelled : LiveSessionEffect

    /** The last thing on the shopping list just landed in the cart: confetti. */
    data object ListCompleted : LiveSessionEffect

    /** A name chip was tapped — the prototype's `pickName()` ends on the price field. */
    data object NamePicked : LiveSessionEffect

    /** The cart holds something, so "Selesai ✓" may open the finish sheet. */
    data object ShowFinishSheet : LiveSessionEffect

    /** The cart holds something, so "Batal" has a loss worth confirming. */
    data object ShowCancelSheet : LiveSessionEffect

    /**
     * Leave the screen. Silent by design: backing out of a trip that bought nothing is not an
     * action worth announcing, and the empty session has already been thrown away.
     */
    data object Left : LiveSessionEffect

    /** A "🏷 merk" chip was tapped — the screen drops it into its note field. */
    data class NoteSuggested(val note: String) : LiveSessionEffect

    data class ShowMessage(val kind: Message) : LiveSessionEffect

    data class ShowError(val failure: Failure) : LiveSessionEffect

    /** Which field the screen shakes and which toast it shows. */
    enum class Message {
        NAME_REQUIRED,
        PRICE_REQUIRED,
        CART_EMPTY,
        ITEM_SAVED,
        ITEM_DELETED,
        REPEAT_HINT,
    }
}
