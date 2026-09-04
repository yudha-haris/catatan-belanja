package com.yudha.catatanbelanja.features.preset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The "Kategori" preset. Deleting a category takes its items with it, which is why the screen
 * asks first and quotes the count — the confirmation is the whole safety net here.
 */
class PresetCategoriesViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PresetCategoriesState())
    val state: StateFlow<PresetCategoriesState> = _state.asStateFlow()

    private val _effects = Channel<PresetCategoriesEffect>(Channel.BUFFERED)
    val effects: Flow<PresetCategoriesEffect> = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            catalogRepository.getCatalog().returnWhen(
                onSuccess = { categories ->
                    _state.update {
                        it.copy(loadState = UiState.Success(Unit), categories = categories)
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** [category] null opens the editor on a new one. */
    fun openEditor(category: CatalogCategory? = null) {
        _state.update { it.copy(isEditorOpen = true, editorCategory = category) }
    }

    fun closeEditor() {
        _state.update { it.copy(isEditorOpen = false, editorCategory = null) }
    }

    fun saveCategory(name: String, emoji: String) {
        if (_state.value.actionState is UiState.Loading) return

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _effects.trySend(PresetCategoriesEffect.NameRequired)
            return
        }

        val editing = _state.value.editorCategory
        val clashes = _state.value.categories
            .any { it.id != editing?.id && it.name.normalized() == trimmedName.normalized() }
        if (clashes) {
            _effects.trySend(PresetCategoriesEffect.DuplicateName)
            return
        }

        // An emoji is decoration; an empty one falls back rather than blocking the save.
        val trimmedEmoji = emoji.trim().ifEmpty { CatalogData.FALLBACK_EMOJI }

        runAction(
            action = {
                when (editing) {
                    null -> catalogRepository.addCategory(trimmedName, trimmedEmoji)
                    else -> catalogRepository.updateCategory(editing.id, trimmedName, trimmedEmoji)
                }
            },
            onDone = {
                closeEditor()
                _effects.send(PresetCategoriesEffect.Saved)
            },
        )
    }

    fun deleteCategory(id: String) = runAction(
        action = { catalogRepository.deleteCategory(id) },
        onDone = {
            closeEditor()
            _effects.send(PresetCategoriesEffect.Deleted)
        },
    )

    fun resetToDefaults() = runAction(
        action = { catalogRepository.resetToDefaults() },
        onDone = {
            closeEditor()
            _effects.send(PresetCategoriesEffect.ResetToDefaults)
        },
    )

    private fun runAction(
        action: suspend () -> Resource<Unit>,
        onDone: suspend () -> Unit,
    ) {
        if (_state.value.actionState is UiState.Loading) return
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            action().returnWhen(
                onSuccess = {
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    onDone()
                    reload()
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    private suspend fun reload() {
        catalogRepository.getCatalog().returnWhen(
            onSuccess = { categories -> _state.update { it.copy(categories = categories) } },
            onError = { failure -> _state.update { it.copy(loadState = UiState.Error(failure)) } },
        )
    }
}
