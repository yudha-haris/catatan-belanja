package com.yudha.catatanbelanja.features.list.presentation

import com.yudha.catatanbelanja.core.common.Failure

sealed interface ShoppingListEffect {
    /** A plan now exists — the screen closes the source sheet and focuses the add field. */
    data class ListStarted(val itemCount: Int) : ShoppingListEffect

    /** Everything on the plan is ticked off: confetti. */
    data object ListCompleted : ShoppingListEffect

    data object TemplateSaved : ShoppingListEffect

    data object TemplateDeleted : ShoppingListEffect

    data object ListDeleted : ShoppingListEffect

    data class ShowMessage(val kind: Message) : ShoppingListEffect

    data class ShowError(val failure: Failure) : ShoppingListEffect

    enum class Message { NAME_REQUIRED, ALREADY_ON_LIST }
}
