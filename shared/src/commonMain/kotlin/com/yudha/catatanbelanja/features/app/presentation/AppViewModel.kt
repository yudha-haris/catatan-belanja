package com.yudha.catatanbelanja.features.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the two things the app needs before it can draw a single screen: the theme flavour and
 * whether a shopping session is still running.
 */
class AppViewModel(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        // The theme and the language follow the database, so picking either in Pengaturan
        // re-draws the app on the spot — no waiting for the screen to close, and an imported
        // or wiped backup lands just as immediately.
        viewModelScope.launch {
            settingsRepository.observeSettings().collectLatest { settings ->
                _state.update {
                    it.copy(themeFlavor = settings.themeFlavor, language = settings.language)
                }
            }
        }
    }

    fun load() {
        // Boots once per process. A configuration change must not rewind the shell to the blank
        // loading screen, because that would take the navigation back stack down with it.
        if (_state.value.loadState !is UiState.Initial) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            val active = when (val result = sessionRepository.getActiveSession()) {
                is Resource.Error -> return@launch failLoad(result.failure)
                is Resource.Success -> result.value
            }

            _state.update {
                it.copy(
                    loadState = UiState.Success(Unit),
                    hasActiveSession = active != null,
                )
            }
        }
    }

    private fun failLoad(failure: Failure) {
        // A failed boot still boots: the default flavour, and no live session to jump into.
        _state.update { it.copy(loadState = UiState.Error(failure)) }
    }
}
