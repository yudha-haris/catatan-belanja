package com.yudha.catatanbelanja.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import com.yudha.catatanbelanja.features.settings.domain.usecase.LoadSettingsOverview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings owns the destructive half of the app, so every data operation goes through
 * [runDataAction]: one at a time, and each one reloads the counts the wipe confirmation quotes.
 * File pickers, share sheets and the clipboard stay outside — [BackupRepository] owns those, and
 * a picked file reaches this view model as plain text via [importFromText].
 */
class SettingsViewModel(
    private val loadSettingsOverview: LoadSettingsOverview,
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            loadSettingsOverview().returnWhen(
                onSuccess = { overview ->
                    _state.update {
                        it.copy(
                            loadState = UiState.Success(Unit),
                            themeFlavor = overview.themeFlavor,
                            sessionCount = overview.sessionCount,
                            stockCount = overview.stockCount,
                        )
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** Re-tapping the active flavour still saves and still toasts, exactly like the prototype. */
    fun changeTheme(flavor: ThemeFlavor) {
        viewModelScope.launch {
            settingsRepository.saveThemeFlavor(flavor).returnWhen(
                onSuccess = {
                    _state.update { it.copy(themeFlavor = flavor) }
                    _effects.send(SettingsEffect.ThemeApplied(flavor))
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun seedDemo() = runDataAction(
        action = { backupRepository.seedDemoData() },
        onDone = {
            _effects.send(SettingsEffect.DemoSeeded)
            _effects.send(SettingsEffect.DataChanged)
        },
    )

    fun exportShare() = runDataAction(
        action = { backupRepository.shareBackup() },
        onDone = { _effects.send(SettingsEffect.ExportShared) },
    )

    fun exportCopy() = runDataAction(
        action = { backupRepository.copyBackupToClipboard() },
        onDone = { _effects.send(SettingsEffect.ExportCopied) },
    )

    /** [raw] is whatever the user pasted or the screen read out of the picked file. */
    fun importFromText(raw: String) {
        if (_state.value.actionState is UiState.Loading) return
        if (raw.isBlank()) {
            viewModelScope.launch { _effects.send(SettingsEffect.ImportRejected) }
            return
        }
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            backupRepository.importFromJson(raw).returnWhen(
                onSuccess = { summary ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(SettingsEffect.ImportMerged(summary))
                    _effects.send(SettingsEffect.DataChanged)
                    reload()
                },
                // The merge itself skips rows it cannot read, so a failure here means the document
                // never parsed. That reads as "Format JSON tidak valid", not as an app error.
                onError = {
                    _state.update { it.copy(actionState = UiState.Initial) }
                    _effects.send(SettingsEffect.ImportRejected)
                },
            )
        }
    }

    fun clearAll() = runDataAction(
        action = { backupRepository.clearAllData() },
        onDone = {
            _effects.send(SettingsEffect.DataCleared)
            _effects.send(SettingsEffect.DataChanged)
        },
    )

    private fun runDataAction(
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

    /** Refreshes the counts after a write without flipping the screen back to its loading state. */
    private suspend fun reload() {
        loadSettingsOverview().returnWhen(
            onSuccess = { overview ->
                _state.update {
                    it.copy(
                        sessionCount = overview.sessionCount,
                        stockCount = overview.stockCount,
                    )
                }
            },
            onError = { failure ->
                _state.update { it.copy(loadState = UiState.Error(failure)) }
            },
        )
    }
}
