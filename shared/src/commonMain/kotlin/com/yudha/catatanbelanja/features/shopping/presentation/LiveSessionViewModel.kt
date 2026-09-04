package com.yudha.catatanbelanja.features.shopping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.common.dataOrNull
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.BrandPreset
import com.yudha.catatanbelanja.core.domain.model.NameChipView
import com.yudha.catatanbelanja.core.domain.repository.BrandRepository
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameChips
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameSuggestions
import com.yudha.catatanbelanja.core.domain.usecase.FindDefaultUnit
import com.yudha.catatanbelanja.features.shopping.domain.usecase.BuildSessionItemViews
import com.yudha.catatanbelanja.features.shopping.domain.usecase.CreateShoppingItem
import com.yudha.catatanbelanja.features.shopping.domain.usecase.CurrentTime
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FindBrandSuggestions
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FindLastPurchase
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FinishShoppingSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiveSessionViewModel(
    private val sessionRepository: SessionRepository,
    private val stockRepository: StockRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val catalogRepository: CatalogRepository,
    private val brandRepository: BrandRepository,
    private val buildNameSuggestions: BuildNameSuggestions,
    private val buildNameChips: BuildNameChips,
    private val findLastPurchase: FindLastPurchase,
    private val findBrandSuggestions: FindBrandSuggestions,
    private val findDefaultUnit: FindDefaultUnit,
    private val buildSessionItemViews: BuildSessionItemViews,
    private val createShoppingItem: CreateShoppingItem,
    private val finishShoppingSession: FinishShoppingSession,
    private val currentTime: CurrentTime,
) : ViewModel() {

    private val _state = MutableStateFlow(LiveSessionState())
    val state: StateFlow<LiveSessionState> = _state.asStateFlow()

    private val _effects = Channel<LiveSessionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // Suggestion inputs. They only change on load, so they are cached instead of re-read per keystroke.
    private var finishedSessions: List<ShoppingSession> = emptyList()
    private var knownNames: List<String> = emptyList()
    private var lowStockNames: List<String> = emptyList()
    private var repeatNames: List<String> = emptyList()
    private var brandPresets: List<BrandPreset> = emptyList()

    /**
     * [repeatFromSessionId] is the session the user tapped "belanja lagi" on — its item names go
     * to the front of the "sering dibeli" chips.
     */
    fun load(repeatFromSessionId: String? = null) {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            val active = when (val result = sessionRepository.getActiveSession()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val finished = when (val result = sessionRepository.getFinishedSessions()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val stock = when (val result = stockRepository.getStockItems()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val list = when (val result = shoppingListRepository.getActiveList()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val categories = when (val result = catalogRepository.getCatalog()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }

            finishedSessions = finished
            knownNames = buildNameSuggestions.knownNames(finishedSessions)
            lowStockNames = stock.filter { it.isLow() }.map { it.name }
            repeatNames = loadRepeatNames(repeatFromSessionId)
            // A missing brand list costs the user a few chips, never the screen — the add form
            // works exactly the same without it.
            brandPresets = brandRepository.getBrands().dataOrNull().orEmpty()

            _state.update {
                it.copy(
                    loadState = UiState.Success(Unit),
                    categoryChips = categories.map { category ->
                        NameChipView(name = category.name, emoji = category.emoji)
                    },
                    frequentNames = buildNameChips(
                        buildNameSuggestions.frequent(
                            sessions = finishedSessions,
                            lowStockNames = lowStockNames,
                            repeatNames = repeatNames,
                        ),
                    ),
                )
            }
            applySession(active)
            applyList(list)
            if (repeatNames.isNotEmpty()) {
                _effects.send(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.REPEAT_HINT))
            }
        }
    }

    fun onNameChanged(query: String) {
        applyNameContext(query = query, isNamePicked = false)
    }

    /**
     * The prototype's `pickName()`: the chip's own `data-v` is the capitalized name, and the
     * caret moves straight on to the price field.
     */
    fun pickName(name: String) {
        val picked = name.trim().capitalizeWords()
        val last = findLastPurchase(picked, finishedSessions)
        _state.update {
            it.copy(
                query = picked,
                isNamePicked = true,
                nameSuggestions = emptyList(),
                showNewItemChip = false,
                lastPurchase = last,
                brandSuggestions = findBrandSuggestions(picked, finishedSessions, brandPresets),
                selectedUnit = last?.unit ?: findDefaultUnit(picked) ?: DEFAULT_UNIT,
            )
        }
        _effects.trySend(LiveSessionEffect.NamePicked)
    }

    fun pickCategory(category: String) {
        val selected = category.takeIf { it != _state.value.selectedCategory }
        _state.update {
            it.copy(
                selectedCategory = selected,
                categoryItems = buildNameChips(
                    catalogRepository.current
                        .firstOrNull { entry -> entry.name == selected }
                        ?.items
                        .orEmpty()
                        .map { item -> item.name },
                ),
            )
        }
    }

    fun pickBrand(note: String) {
        _effects.trySend(LiveSessionEffect.NoteSuggested(note))
    }

    fun pickUnit(unit: String) {
        _state.update { it.copy(selectedUnit = unit) }
    }

    /**
     * The "Pakai" shortcut: reuse the last purchase's price, quantity, unit and brand. The
     * prototype only overwrites the Jumlah field when that purchase actually carried a quantity,
     * so whatever the user typed survives when it did not.
     */
    fun useLastPrice(note: String, qtyText: String) {
        val last = _state.value.lastPurchase ?: return

        submitItem(
            name = _state.value.query,
            qty = last.qty ?: qtyText.toQty(),
            unit = last.unit ?: _state.value.selectedUnit,
            note = note.ifBlank { last.note },
            price = last.price,
        )
    }

    fun addItem(name: String, qtyText: String, unit: String, note: String, priceText: String) {
        submitItem(
            name = name,
            qty = qtyText.toQty(),
            unit = unit,
            note = note,
            price = priceText.toRupiahAmount(),
        )
    }

    fun updateItem(
        itemId: String,
        name: String,
        qtyText: String,
        unit: String,
        note: String,
        priceText: String,
    ) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _effects.trySend(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.NAME_REQUIRED))
            return
        }

        val qty = qtyText.toQty()?.takeIf { it > 0.0 }
        val item = ShoppingItem(
            id = itemId,
            name = trimmedName.capitalizeWords(),
            price = priceText.toRupiahAmount(),
            qty = qty,
            unit = unit.takeIf { qty != null },
            note = note.trim(),
        )
        runAction(LiveSessionEffect.Message.ITEM_SAVED) {
            sessionRepository.updateItem(session.id, item)
        }
    }

    fun deleteItem(itemId: String) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return
        val removedName = session.items.firstOrNull { it.id == itemId }?.name

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.deleteItem(session.id, itemId).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    refreshSession()
                    if (removedName != null) untickList(removedName)
                    _effects.send(
                        LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.ITEM_DELETED),
                    )
                },
                onError = { failAction(it) },
            )
        }
    }

    /**
     * The paper receipt, photographed at the till. [bytes] arrive already scaled and encoded: the
     * picture came from a camera or a gallery, both of which are screen concerns, and neither the
     * ViewModel nor the repository is in a position to know how big a JPEG the shot deserves.
     */
    fun attachReceiptPhoto(bytes: ByteArray) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return

        runAction(LiveSessionEffect.Message.PHOTO_ATTACHED) {
            sessionRepository.attachReceiptPhoto(session.id, bytes)
        }
    }

    fun removeReceiptPhoto() {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return

        runAction(LiveSessionEffect.Message.PHOTO_REMOVED) {
            sessionRepository.removeReceiptPhoto(session.id)
        }
    }

    fun updateStore(store: String) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return

        runAction(null) { sessionRepository.updateStore(session.id, store.trim()) }
    }

    /** The prototype refuses to open `finishSheet()` at all while the cart is empty. */
    fun openFinishSheet() {
        val session = _state.value.session ?: return
        if (session.items.isEmpty()) {
            _effects.trySend(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.CART_EMPTY))
            return
        }

        _state.update { it.copy(finishedAtMillis = currentTime()) }
        _effects.trySend(LiveSessionEffect.ShowFinishSheet)
    }

    fun finishSession(name: String, addToStock: Boolean, carryOverList: Boolean) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return
        if (session.items.isEmpty()) {
            _effects.trySend(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.CART_EMPTY))
            return
        }

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            finishShoppingSession(
                session = session,
                name = name.trim(),
                addToStock = addToStock,
                carryOverList = carryOverList,
            ).returnWhen(
                onSuccess = { result ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(
                        LiveSessionEffect.Finished(
                            sessionId = session.id,
                            addedToStock = result.addedToStock,
                            carriedOverToList = result.carriedOverToList,
                        ),
                    )
                },
                onError = { failAction(it) },
            )
        }
    }

    /**
     * "Batal" with something in the cart is a real loss, so it confirms first — the prototype's
     * `cancelSheet()`. An empty cart has nothing to confirm, so it just goes.
     */
    fun requestCancel() {
        val session = _state.value.session ?: return
        if (session.items.isEmpty()) {
            cancelSession()
            return
        }

        _effects.trySend(LiveSessionEffect.ShowCancelSheet)
    }

    /**
     * Backing out of the screen. A session that bought nothing was only ever an empty container,
     * so it is thrown away rather than left on the home screen as a trip the user never took.
     * A cart with something in it is left running, which is what "Sedang belanja" is for.
     */
    fun leaveSession() {
        if (_state.value.actionState is UiState.Loading) return
        val session = _state.value.session
        if (session == null || session.items.isNotEmpty()) {
            _effects.trySend(LiveSessionEffect.Left)
            return
        }

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.cancelActiveSession().returnWhen(
                onSuccess = { _ -> _state.update { it.copy(actionState = UiState.Success(Unit)) } },
                // The delete failing leaves exactly what today's build leaves behind, so it is
                // reported and the user still gets to leave rather than being held on the screen.
                onError = { failure -> failAction(failure) },
            )
            _effects.send(LiveSessionEffect.Left)
        }
    }

    fun cancelSession() {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.cancelActiveSession().returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(LiveSessionEffect.Cancelled)
                },
                onError = { failAction(it) },
            )
        }
    }

    private fun submitItem(name: String, qty: Double?, unit: String, note: String, price: Int) {
        val session = _state.value.session ?: return
        if (_state.value.actionState is UiState.Loading) return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _effects.trySend(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.NAME_REQUIRED))
            return
        }
        if (price <= 0) {
            _effects.trySend(LiveSessionEffect.ShowMessage(LiveSessionEffect.Message.PRICE_REQUIRED))
            return
        }

        val item = createShoppingItem(
            name = trimmedName,
            qty = qty,
            unit = unit,
            note = note,
            price = price,
        )
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.addItem(session.id, item).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    resetInput()
                    refreshSession()
                    _effects.send(LiveSessionEffect.ItemAdded(item.name, item.note, item.price))
                    tickOffList(item.name)
                },
                onError = { failAction(it) },
            )
        }
    }

    private fun runAction(
        message: LiveSessionEffect.Message?,
        block: suspend () -> Resource<Unit>,
    ) {
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            block().returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    refreshSession()
                    message?.let { _effects.send(LiveSessionEffect.ShowMessage(it)) }
                },
                onError = { failAction(it) },
            )
        }
    }

    private suspend fun refreshSession() {
        sessionRepository.getActiveSession().returnWhen(
            onSuccess = { applySession(it) },
            onError = { _effects.send(LiveSessionEffect.ShowError(it)) },
        )
    }

    private fun applySession(session: ShoppingSession?) {
        _state.update {
            it.copy(
                session = session,
                total = session?.items?.sumOf { item -> item.price } ?: 0,
                itemCount = session?.items?.size ?: 0,
                lastItemName = session?.items?.firstOrNull()?.name,
                itemViews = buildSessionItemViews(session?.items.orEmpty()),
                stockableCount = session?.items?.count { item -> item.qty != null } ?: 0,
            )
        }
    }

    /**
     * Ticks the bought item off the plan. The completion effect fires only on the transition, so
     * adding a second bag of rice to an already-finished list does not celebrate twice.
     */
    private suspend fun tickOffList(name: String) {
        if (!_state.value.hasList) return

        val wasComplete = _state.value.isListComplete
        // A plan that will not tick off is not worth interrupting a shopping trip over: the item
        // is already in the cart either way, so a failure here just leaves the line unticked.
        val updated = shoppingListRepository.checkItemByName(name).dataOrNull() ?: return
        applyList(updated)
        if (wasComplete) return
        if (!_state.value.isListComplete) return

        _effects.send(LiveSessionEffect.ListCompleted)
    }

    /**
     * Taking the item back out of the cart un-ticks its line — but only once the cart holds no
     * other item by that name, so buying two bags of rice and removing one keeps the line crossed
     * off. Best-effort, like the tick itself.
     */
    private suspend fun untickList(name: String) {
        if (!_state.value.hasList) return
        val key = name.normalized()
        if (_state.value.session?.items.orEmpty().any { it.name.normalized() == key }) return

        val updated = shoppingListRepository.uncheckItemByName(name).dataOrNull() ?: return
        applyList(updated)
    }

    private fun applyList(list: ShoppingList?) {
        val items = list?.items.orEmpty()
        val total = items.size
        val remaining = items.filterNot { it.isChecked }
        _state.update {
            it.copy(
                hasList = list != null && total > 0,
                listRemaining = buildNameChips(remaining.map { item -> item.name }),
                listPreview = buildNameChips(
                    remaining.take(LIST_PREVIEW_LIMIT).map { item -> item.name },
                ),
                listHiddenCount = (remaining.size - LIST_PREVIEW_LIMIT).coerceAtLeast(0),
                listTotalCount = total,
                listCheckedCount = total - remaining.size,
                listRemainingCount = remaining.size,
                listProgress = when (total) {
                    0 -> 0f
                    else -> (total - remaining.size).toFloat() / total.toFloat()
                },
                isListComplete = total > 0 && remaining.isEmpty(),
            )
        }
    }

    private fun applyNameContext(query: String, isNamePicked: Boolean) {
        val trimmed = query.trim()
        val searching = !isNamePicked && trimmed.isNotEmpty()
        _state.update {
            it.copy(
                query = query,
                isNamePicked = isNamePicked,
                nameSuggestions = when (searching) {
                    true -> buildNameChips(buildNameSuggestions(trimmed, knownNames))
                    false -> emptyList()
                },
                showNewItemChip = searching && !buildNameSuggestions.hasExactMatch(trimmed, knownNames),
                lastPurchase = findLastPurchase(trimmed, finishedSessions),
                brandSuggestions = findBrandSuggestions(trimmed, finishedSessions, brandPresets),
            )
        }
    }

    private fun resetInput() {
        _state.update {
            it.copy(
                query = "",
                isNamePicked = false,
                nameSuggestions = emptyList(),
                showNewItemChip = false,
                lastPurchase = null,
                brandSuggestions = emptyList(),
                selectedCategory = null,
                categoryItems = emptyList(),
                selectedUnit = DEFAULT_UNIT,
            )
        }
    }

    private suspend fun loadRepeatNames(sessionId: String?): List<String> {
        if (sessionId == null) return emptyList()

        return sessionRepository.getSession(sessionId).returnWhen(
            onSuccess = { session -> session?.items.orEmpty().map { it.name } },
            onError = { emptyList() },
        )
    }

    private suspend fun failLoad(error: Resource.Error) {
        _state.update { it.copy(loadState = UiState.Error(error.failure)) }
        _effects.send(LiveSessionEffect.ShowError(error.failure))
    }

    private suspend fun failAction(failure: Failure) {
        _state.update { it.copy(actionState = UiState.Error(failure)) }
        _effects.send(LiveSessionEffect.ShowError(failure))
    }

    private fun StockItem.isLow(): Boolean = minQty?.let { qty <= it } ?: (qty <= 0.0)

    /** "12.500" / "Rp12.500" -> 12500. Only the digits carry meaning. */
    private fun String.toRupiahAmount(): Int = filter { it.isDigit() }.toIntOrNull() ?: 0

    /** "1,5" and "1.5" both mean one and a half. */
    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()

    private companion object {
        val DEFAULT_UNIT: String = CatalogData.units.first()

        /** How many plan chips the strip shows before it collapses the rest behind "+n lagi". */
        const val LIST_PREVIEW_LIMIT = 8
    }
}
