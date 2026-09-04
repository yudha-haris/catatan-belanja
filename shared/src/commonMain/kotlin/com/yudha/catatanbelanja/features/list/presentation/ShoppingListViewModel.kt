package com.yudha.catatanbelanja.features.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameChips
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameSuggestions
import com.yudha.catatanbelanja.features.list.domain.model.ListSource
import com.yudha.catatanbelanja.features.list.domain.usecase.BuildListItemViews
import com.yudha.catatanbelanja.features.list.domain.usecase.BuildListSources
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The Daftar screen: write down what the trip should buy, then tick it off.
 *
 * Everything here is one tap deep on purpose. A list is names only — no price, no quantity —
 * because a planning screen that asks for numbers is a planning screen nobody fills in.
 */
class ShoppingListViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val sessionRepository: SessionRepository,
    private val stockRepository: StockRepository,
    private val buildListItemViews: BuildListItemViews,
    private val buildListSources: BuildListSources,
    private val buildNameSuggestions: BuildNameSuggestions,
    private val buildNameChips: BuildNameChips,
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingListState())
    val state: StateFlow<ShoppingListState> = _state.asStateFlow()

    private val _effects = Channel<ShoppingListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // Suggestion inputs. They only change on load, so they are cached rather than re-read per
    // keystroke, exactly as the live session caches them.
    private var knownNames: List<String> = emptyList()
    private var frequentNames: List<String> = emptyList()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            val finished = when (val result = sessionRepository.getFinishedSessions()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val stock = when (val result = stockRepository.getStockItems()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val templates = when (val result = shoppingListRepository.getTemplates()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val active = when (val result = shoppingListRepository.getActiveList()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }

            knownNames = buildNameSuggestions.knownNames(finished)
            frequentNames = buildFrequentNames(finished, stock)

            _state.update {
                it.copy(
                    loadState = UiState.Success(Unit),
                    sources = buildListSources(
                        lastSession = finished.firstOrNull(),
                        stock = stock,
                        templates = templates,
                    ),
                )
            }
            applyList(active)
        }
    }

    fun onQueryChanged(query: String) {
        val trimmed = query.trim()
        val searching = trimmed.isNotEmpty()
        _state.update {
            it.copy(
                query = query,
                searchChips = when (searching) {
                    true -> buildNameChips(buildNameSuggestions(trimmed, knownNames))
                    false -> emptyList()
                },
                showNewItemChip = searching &&
                    !buildNameSuggestions.hasExactMatch(trimmed, knownNames),
            )
        }
    }

    /** The add field's "＋" and its keyboard Done action both land here. */
    fun addTyped() {
        addName(_state.value.query)
    }

    fun addName(name: String) {
        val listId = _state.value.listId ?: return
        if (_state.value.actionState is UiState.Loading) return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _effects.trySend(ShoppingListEffect.ShowMessage(ShoppingListEffect.Message.NAME_REQUIRED))
            return
        }

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.addItems(listId, listOf(trimmed)).returnWhen(
                onSuccess = { added ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    clearQuery()
                    refreshList()
                    // Landing quietly is the point: writing a list is a burst of six or eight
                    // items, and a pill per item would be six pills. The new row and the
                    // counter say it worked; only the no-op needs explaining.
                    if (added > 0) return@returnWhen

                    _effects.send(
                        ShoppingListEffect.ShowMessage(ShoppingListEffect.Message.ALREADY_ON_LIST),
                    )
                },
                onError = { failAction(it) },
            )
        }
    }

    /**
     * Ticking the last unticked line is the moment the feature pays off, so it celebrates. The
     * check is against the list as it now stands, not the count before the write.
     */
    fun toggleItem(itemId: String, isChecked: Boolean) {
        val listId = _state.value.listId ?: return
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.setItemChecked(listId, itemId, isChecked).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    val wasComplete = _state.value.isComplete
                    refreshList()
                    if (wasComplete) return@returnWhen
                    if (!_state.value.isComplete) return@returnWhen

                    _effects.send(ShoppingListEffect.ListCompleted)
                },
                onError = { failAction(it) },
            )
        }
    }

    fun removeItem(itemId: String) {
        val listId = _state.value.listId ?: return
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.deleteItem(listId, itemId).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    refreshList()
                },
                onError = { failAction(it) },
            )
        }
    }

    fun startList(source: ListSource) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.startList(source.names).returnWhen(
                onSuccess = { list ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    clearQuery()
                    applyList(list)
                    _effects.send(ShoppingListEffect.ListStarted(list.items.size))
                },
                onError = { failAction(it) },
            )
        }
    }

    fun saveAsTemplate(name: String) {
        val listId = _state.value.listId ?: return
        if (_state.value.actionState is UiState.Loading) return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _effects.trySend(ShoppingListEffect.ShowMessage(ShoppingListEffect.Message.NAME_REQUIRED))
            return
        }

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.saveAsTemplate(listId, trimmed).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(ShoppingListEffect.TemplateSaved)
                    load()
                },
                onError = { failAction(it) },
            )
        }
    }

    /**
     * Templates accumulate, so they have to be removable. Deleting one only drops the saved copy
     * — the plan it was made from, if there still is one, is untouched.
     */
    fun deleteTemplate(templateId: String) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.deleteList(templateId).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(ShoppingListEffect.TemplateDeleted)
                    load()
                },
                onError = { failAction(it) },
            )
        }
    }

    fun deleteList() {
        val listId = _state.value.listId ?: return
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            shoppingListRepository.deleteList(listId).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    clearQuery()
                    applyList(null)
                    _effects.send(ShoppingListEffect.ListDeleted)
                },
                onError = { failAction(it) },
            )
        }
    }

    private suspend fun refreshList() {
        shoppingListRepository.getActiveList().returnWhen(
            onSuccess = { applyList(it) },
            onError = { _effects.send(ShoppingListEffect.ShowError(it)) },
        )
    }

    private fun applyList(list: ShoppingList?) {
        val items = list?.items.orEmpty()
        val total = items.size
        val checked = items.count { it.isChecked }
        val onList = items.mapTo(mutableSetOf()) { it.name.normalized() }

        _state.update {
            it.copy(
                listId = list?.id,
                hasList = list != null,
                itemViews = buildListItemViews(items),
                totalCount = total,
                checkedCount = checked,
                remainingCount = total - checked,
                progress = when (total) {
                    0 -> 0f
                    else -> checked.toFloat() / total.toFloat()
                },
                isComplete = total > 0 && checked == total,
                // A chip for something already written down would do nothing, so it is dropped
                // rather than shown and then refused.
                quickAddChips = buildNameChips(
                    frequentNames.filterNot { name -> name.normalized() in onList },
                ),
            )
        }
    }

    /** Running low first — that is the most likely reason to be writing a list at all. */
    private fun buildFrequentNames(
        sessions: List<ShoppingSession>,
        stock: List<StockItem>,
    ): List<String> = buildNameSuggestions.frequent(
        sessions = sessions,
        lowStockNames = stock.filter { it.isLow() }.map { it.name },
        repeatNames = emptyList(),
    )

    private fun clearQuery() {
        _state.update { it.copy(query = "", searchChips = emptyList(), showNewItemChip = false) }
    }

    private suspend fun failLoad(error: Resource.Error) {
        _state.update { it.copy(loadState = UiState.Error(error.failure)) }
        _effects.send(ShoppingListEffect.ShowError(error.failure))
    }

    private suspend fun failAction(failure: Failure) {
        _state.update { it.copy(actionState = UiState.Error(failure)) }
        _effects.send(ShoppingListEffect.ShowError(failure))
    }

    private fun StockItem.isLow(): Boolean = minQty?.let { qty <= it } ?: (qty <= 0.0)
}
