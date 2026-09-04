package com.yudha.catatanbelanja.features.preset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The "Bahasa" preset. Saving is all this does: the shell observes the settings row and re-draws
 * the whole app in the new language, so there is nothing here to apply by hand.
 */
class PresetLanguageViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PresetLanguageState())
    val state: StateFlow<PresetLanguageState> = _state.asStateFlow()

    private val _effects = Channel<PresetLanguageEffect>(Channel.BUFFERED)
    val effects: Flow<PresetLanguageEffect> = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            settingsRepository.getSettings().returnWhen(
                onSuccess = { settings ->
                    _state.update {
                        it.copy(loadState = UiState.Success(Unit), language = settings.language)
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** Re-tapping the active language still saves and still toasts, like the theme picker does. */
    fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.saveLanguage(language).returnWhen(
                onSuccess = {
                    _state.update { it.copy(language = language) }
                    _effects.send(PresetLanguageEffect.LanguageApplied(language))
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }
}
