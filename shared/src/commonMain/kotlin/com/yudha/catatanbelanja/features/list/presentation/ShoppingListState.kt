package com.yudha.catatanbelanja.features.list.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.NameChipView
import com.yudha.catatanbelanja.features.list.domain.model.ListSource
import com.yudha.catatanbelanja.features.list.domain.model.ShoppingListItemView

/**
 * The Daftar screen. Two shapes in one state: with no plan yet it is the [sources] menu, and
 * with one it is the checklist. [hasList] is what the screen switches on.
 */
data class ShoppingListState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,

    // the plan
    val listId: String? = null,
    val hasList: Boolean = false,
    val itemViews: List<ShoppingListItemView> = emptyList(),
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
    val remainingCount: Int = 0,
    /** 0f..1f, ready for the progress bar — the screen never divides. */
    val progress: Float = 0f,
    val isComplete: Boolean = false,

    // the add field
    val query: String = "",
    val searchChips: List<NameChipView> = emptyList(),
    /** Nothing known matches what is being typed, so the "＋ tambah" chip offers it as new. */
    val showNewItemChip: Boolean = false,
    /** Frequent and running-low names, minus whatever the plan already holds. */
    val quickAddChips: List<NameChipView> = emptyList(),

    // the "buat daftar" menu
    val sources: List<ListSource> = emptyList(),
)
