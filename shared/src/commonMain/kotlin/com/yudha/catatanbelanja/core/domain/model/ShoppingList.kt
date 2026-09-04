package com.yudha.catatanbelanja.core.domain.model

/**
 * What the next trip is *supposed* to buy — the note the user would otherwise keep in WhatsApp.
 *
 * Exactly one list is "active" at a time ([isTemplate] false, [archivedAt] null): the plan for
 * the next trip. Finishing a session archives it. A template is the same shape kept for reuse,
 * never active, and never ticked off.
 */
data class ShoppingList(
    val id: String,
    val name: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val isTemplate: Boolean = false,
    val archivedAt: Long? = null,
    val items: List<ShoppingListItem> = emptyList(),
)
