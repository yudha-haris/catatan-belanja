package com.yudha.catatanbelanja.android.screen.shopping

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * The add-item form's own UI state: the buffers the ViewModel only hears about on submit, the
 * focus flow (nama → harga → tambah) and the two shake counters. Held with `remember` by
 * `LiveSessionScreen`; the name itself lives in the ViewModel because the suggestions follow it.
 */
@Stable
internal class LiveAddForm {
    var qty by mutableStateOf("")
    var note by mutableStateOf("")
    var price by mutableIntStateOf(0)

    /** Bumped to replay the shake — a boolean could not fire twice in a row. */
    var nameShake by mutableIntStateOf(0)
        private set
    var priceShake by mutableIntStateOf(0)
        private set

    val nameFocus = FocusRequester()
    val qtyFocus = FocusRequester()
    val noteFocus = FocusRequester()
    val priceFocus = FocusRequester()

    fun shakeName() {
        nameShake += 1
        nameFocus.requestFocus()
    }

    fun shakePrice() {
        priceShake += 1
        priceFocus.requestFocus()
    }

    /** After an item lands in the cart the prototype clears everything but the keyboard. */
    fun clear() {
        qty = ""
        note = ""
        price = 0
    }
}
