package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory

data class PresetCategoriesState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val categories: List<CatalogCategory> = emptyList(),
    val isEditorOpen: Boolean = false,
    /** Null while the editor is adding rather than editing. */
    val editorCategory: CatalogCategory? = null,
)
