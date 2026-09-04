package com.yudha.catatanbelanja.features.preset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.model.CatalogItem
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import com.yudha.catatanbelanja.features.preset.domain.model.PresetItemSection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The "Belanjaan" preset: every catalog item, grouped under its category and searchable, with one
 * sheet for adding and editing. Moving an item between categories is the same save — the sheet
 * carries a category picker, so nothing has to be deleted and retyped.
 */
class PresetItemsViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PresetItemsState())
    val state: StateFlow<PresetItemsState> = _state.asStateFlow()

    private val _effects = Channel<PresetItemsEffect>(Channel.BUFFERED)
    val effects: Flow<PresetItemsEffect> = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            catalogRepository.getCatalog().returnWhen(
                onSuccess = { categories ->
                    _state.update { it.withCatalog(categories).copy(loadState = UiState.Success(Unit)) }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.withQuery(query) }
    }

    /** [item] null opens the editor on a new row, filed under [categoryId]. */
    fun openEditor(item: CatalogItem? = null, categoryId: String = "") {
        val fallbackCategory = categoryId.ifEmpty {
            _state.value.categories.firstOrNull()?.id.orEmpty()
        }
        _state.update {
            it.copy(
                isEditorOpen = true,
                editorItem = item,
                editorCategoryId = item?.categoryId ?: fallbackCategory,
                editorUnit = item?.defaultUnit.orEmpty(),
            )
        }
    }

    fun closeEditor() {
        _state.update {
            it.copy(isEditorOpen = false, editorItem = null, editorCategoryId = "", editorUnit = "")
        }
    }

    fun pickEditorCategory(categoryId: String) {
        _state.update { it.copy(editorCategoryId = categoryId) }
    }

    /** An empty [unit] is a real answer: it means the catalog has no default for this item. */
    fun pickEditorUnit(unit: String) {
        _state.update { it.copy(editorUnit = unit) }
    }

    fun saveItem(name: String) {
        if (_state.value.actionState is UiState.Loading) return

        val current = _state.value
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _effects.trySend(PresetItemsEffect.NameRequired)
            return
        }
        if (current.editorCategoryId.isEmpty()) {
            _effects.trySend(PresetItemsEffect.CategoryRequired)
            return
        }

        val editing = current.editorItem
        val clashes = current.categories
            .asSequence()
            .flatMap { it.items.asSequence() }
            .any { it.id != editing?.id && it.name.normalized() == trimmed.normalized() }
        if (clashes) {
            _effects.trySend(PresetItemsEffect.DuplicateName)
            return
        }

        runAction(
            action = {
                when (editing) {
                    null -> catalogRepository.addItem(
                        categoryId = current.editorCategoryId,
                        name = trimmed,
                        defaultUnit = current.editorUnit,
                    )

                    else -> catalogRepository.updateItem(
                        id = editing.id,
                        categoryId = current.editorCategoryId,
                        name = trimmed,
                        defaultUnit = current.editorUnit,
                    )
                }
            },
            onDone = {
                closeEditor()
                _effects.send(PresetItemsEffect.Saved)
            },
        )
    }

    fun deleteItem(id: String) = runAction(
        action = { catalogRepository.deleteItem(id) },
        onDone = {
            closeEditor()
            _effects.send(PresetItemsEffect.Deleted)
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
            onSuccess = { categories -> _state.update { it.withCatalog(categories) } },
            onError = { failure -> _state.update { it.copy(loadState = UiState.Error(failure)) } },
        )
    }

    private fun PresetItemsState.withCatalog(categories: List<CatalogCategory>): PresetItemsState =
        copy(categories = categories).withQuery(query)

    /** The one place [PresetItemsState.sections] is derived — state carries no computed properties. */
    private fun PresetItemsState.withQuery(query: String): PresetItemsState {
        val key = query.normalized()
        val sections = categories.mapNotNull { category ->
            val items = when (key.isEmpty()) {
                true -> category.items
                false -> category.items.filter { it.name.normalized().contains(key) }
            }
            if (items.isEmpty()) return@mapNotNull null
            PresetItemSection(
                categoryId = category.id,
                categoryName = category.name,
                emoji = category.emoji,
                items = items,
            )
        }
        val total = categories.sumOf { it.items.size }
        return copy(
            query = query,
            sections = sections,
            totalCount = total,
            isSearchEmpty = total > 0 && sections.isEmpty(),
        )
    }
}
