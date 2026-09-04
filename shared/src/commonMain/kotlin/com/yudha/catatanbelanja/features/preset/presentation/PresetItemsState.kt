package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.model.CatalogItem
import com.yudha.catatanbelanja.features.preset.domain.model.PresetItemSection

/**
 * [sections] is [categories] filtered by [query] and stripped of the categories that matched
 * nothing — the composable draws it as-is and does no filtering of its own.
 */
data class PresetItemsState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val categories: List<CatalogCategory> = emptyList(),
    val sections: List<PresetItemSection> = emptyList(),
    val query: String = "",
    val totalCount: Int = 0,
    /** True when the catalog holds items but [query] matched none of them. */
    val isSearchEmpty: Boolean = false,

    // add / edit sheet
    val isEditorOpen: Boolean = false,
    /** Null while the editor is adding rather than editing. */
    val editorItem: CatalogItem? = null,
    val editorCategoryId: String = "",
    val editorUnit: String = "",
    val units: List<String> = CatalogData.units,
)
