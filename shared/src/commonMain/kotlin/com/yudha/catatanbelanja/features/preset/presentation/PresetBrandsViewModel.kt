package com.yudha.catatanbelanja.features.preset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.BrandPreset
import com.yudha.catatanbelanja.core.domain.repository.BrandRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The "Merk" preset: one flat list, added to and edited from a single sheet. */
class PresetBrandsViewModel(
    private val brandRepository: BrandRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PresetBrandsState())
    val state: StateFlow<PresetBrandsState> = _state.asStateFlow()

    private val _effects = Channel<PresetBrandsEffect>(Channel.BUFFERED)
    val effects: Flow<PresetBrandsEffect> = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            brandRepository.getBrands().returnWhen(
                onSuccess = { brands ->
                    _state.update {
                        it.copy(loadState = UiState.Success(Unit), brands = brands)
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** [brand] null opens the editor on a new row. */
    fun openEditor(brand: BrandPreset? = null) {
        _state.update { it.copy(isEditorOpen = true, editorBrand = brand) }
    }

    fun closeEditor() {
        _state.update { it.copy(isEditorOpen = false, editorBrand = null) }
    }

    fun saveBrand(name: String) {
        if (_state.value.actionState is UiState.Loading) return

        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _effects.trySend(PresetBrandsEffect.NameRequired)
            return
        }

        val editing = _state.value.editorBrand
        val clashes = _state.value.brands
            .any { it.id != editing?.id && it.name.normalized() == trimmed.normalized() }
        if (clashes) {
            _effects.trySend(PresetBrandsEffect.DuplicateName)
            return
        }

        runAction(
            action = {
                when (editing) {
                    null -> brandRepository.addBrand(trimmed)
                    else -> brandRepository.renameBrand(editing.id, trimmed)
                }
            },
            onDone = {
                closeEditor()
                _effects.send(PresetBrandsEffect.Saved)
            },
        )
    }

    fun deleteBrand(id: String) = runAction(
        action = { brandRepository.deleteBrand(id) },
        onDone = {
            closeEditor()
            _effects.send(PresetBrandsEffect.Deleted)
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

    /** Refreshes the list after a write without flipping the screen back to its loading state. */
    private suspend fun reload() {
        brandRepository.getBrands().returnWhen(
            onSuccess = { brands -> _state.update { it.copy(brands = brands) } },
            onError = { failure -> _state.update { it.copy(loadState = UiState.Error(failure)) } },
        )
    }
}
