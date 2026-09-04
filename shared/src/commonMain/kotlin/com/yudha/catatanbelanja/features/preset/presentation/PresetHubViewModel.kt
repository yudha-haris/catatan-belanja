package com.yudha.catatanbelanja.features.preset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.features.preset.domain.usecase.LoadPresetOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The four preset rows and what each of them holds. It reloads on every return from a sub-screen,
 * which is what keeps the counts honest after an edit.
 */
class PresetHubViewModel(
    private val loadPresetOverview: LoadPresetOverview,
) : ViewModel() {

    private val _state = MutableStateFlow(PresetHubState())
    val state: StateFlow<PresetHubState> = _state.asStateFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            loadPresetOverview().returnWhen(
                onSuccess = { overview ->
                    _state.update {
                        it.copy(
                            loadState = UiState.Success(Unit),
                            itemCount = overview.itemCount,
                            categoryCount = overview.categoryCount,
                            brandCount = overview.brandCount,
                            language = overview.language,
                        )
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }
}
