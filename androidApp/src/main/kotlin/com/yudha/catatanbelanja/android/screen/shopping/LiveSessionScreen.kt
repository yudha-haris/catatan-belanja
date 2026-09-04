package com.yudha.catatanbelanja.android.screen.shopping

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.ReceiptHeader
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toTimeLabel
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveAddItemCard
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveCartEmptyState
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveCartRow
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveFinishSheet
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveItemSheet
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveListStrip
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveSessionBottomBar
import com.yudha.catatanbelanja.android.screen.shopping.components.LiveStoreSheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.shopping.domain.model.ShoppingItemView
import com.yudha.catatanbelanja.features.shopping.presentation.LiveSessionEffect
import com.yudha.catatanbelanja.features.shopping.presentation.LiveSessionViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The live session — the prototype's `liveView()`. A pushed route: no tab bar, and the dark
 * total bar stays pinned over the scrolling receipt, add form and cart.
 */
@Composable
fun LiveSessionScreen(
    onBack: () -> Unit,
    onSessionFinished: (String) -> Unit,
    modifier: Modifier = Modifier,
    repeatFromSessionId: String? = null,
    viewModel: LiveSessionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val context = LocalContext.current
    val form = remember { LiveAddForm() }

    var editingItem by remember { mutableStateOf<ShoppingItemView?>(null) }
    var showStoreSheet by remember { mutableStateOf(false) }
    var showFinishSheet by remember { mutableStateOf(false) }
    var showCancelSheet by remember { mutableStateOf(false) }
    // Saveable, not remembered: the strip lives inside the LazyColumn, so a plain remember
    // would collapse it again every time it scrolled out of view.
    var isListExpanded by rememberSaveable { mutableStateOf(false) }
    // Someone who wants quantities wants them for the whole trip, so the optional fields
    // stay open once opened rather than folding back after every item.
    var isDetailOpen by rememberSaveable { mutableStateOf(false) }

    val needNameMessage = stringResource(R.string.live_toast_need_name)
    val needPriceMessage = stringResource(R.string.live_toast_need_price)
    val cartEmptyMessage = stringResource(R.string.live_toast_cart_empty)
    val itemSavedMessage = stringResource(R.string.common_item_toast_saved)
    val itemDeletedMessage = stringResource(R.string.common_item_toast_deleted)
    val repeatHintMessage = stringResource(R.string.live_toast_repeat_hint)
    val cancelledMessage = stringResource(R.string.live_toast_cancelled)
    val savedMessage = stringResource(R.string.live_toast_saved)
    val listCompleteMessage = stringResource(R.string.live_toast_list_complete)

    LaunchedEffect(Unit) {
        viewModel.load(repeatFromSessionId)
        viewModel.effects.collect { effect ->
            when (effect) {
                is LiveSessionEffect.ItemAdded -> {
                    form.clear()
                    form.nameFocus.requestFocus()
                    val toast = when (effect.note.isEmpty()) {
                        true -> context.getString(
                            R.string.live_toast_item_added,
                            effect.name,
                            effect.price.toRupiah(),
                        )

                        false -> context.getString(
                            R.string.live_toast_item_added_with_note,
                            effect.name,
                            effect.note,
                            effect.price.toRupiah(),
                        )
                    }
                    appUi.showToast(toast)
                }

                is LiveSessionEffect.Finished -> {
                    showFinishSheet = false
                    appUi.celebrate()
                    val toast = when {
                        effect.carriedOverToList > 0 -> context.resources.getQuantityString(
                            R.plurals.live_toast_saved_with_carry,
                            effect.carriedOverToList,
                            effect.carriedOverToList,
                        )

                        effect.addedToStock > 0 -> context.resources.getQuantityString(
                            R.plurals.live_toast_saved_with_stock,
                            effect.addedToStock,
                            effect.addedToStock,
                        )

                        else -> savedMessage
                    }
                    appUi.showToast(toast)
                    onSessionFinished(effect.sessionId)
                }

                LiveSessionEffect.ListCompleted -> {
                    appUi.celebrate()
                    appUi.showToast(listCompleteMessage)
                }

                LiveSessionEffect.Cancelled -> {
                    showCancelSheet = false
                    appUi.showToast(cancelledMessage)
                    onBack()
                }

                // `pickName()` ends on the price field so the amount can be typed straight away.
                LiveSessionEffect.NamePicked -> form.priceFocus.requestFocus()

                LiveSessionEffect.ShowFinishSheet -> showFinishSheet = true

                LiveSessionEffect.ShowCancelSheet -> showCancelSheet = true

                LiveSessionEffect.Left -> onBack()

                is LiveSessionEffect.NoteSuggested -> {
                    form.note = effect.note
                    form.priceFocus.requestFocus()
                }

                is LiveSessionEffect.ShowMessage -> when (effect.kind) {
                    LiveSessionEffect.Message.NAME_REQUIRED -> {
                        form.shakeName()
                        appUi.showToast(needNameMessage)
                    }

                    LiveSessionEffect.Message.PRICE_REQUIRED -> {
                        form.shakePrice()
                        appUi.showToast(needPriceMessage)
                    }

                    LiveSessionEffect.Message.CART_EMPTY -> appUi.showToast(cartEmptyMessage)

                    LiveSessionEffect.Message.ITEM_SAVED -> {
                        editingItem = null
                        appUi.showToast(itemSavedMessage)
                    }

                    LiveSessionEffect.Message.ITEM_DELETED -> {
                        editingItem = null
                        appUi.showToast(itemDeletedMessage)
                    }

                    LiveSessionEffect.Message.REPEAT_HINT -> appUi.showToast(repeatHintMessage)
                }

                is LiveSessionEffect.ShowError -> appUi.showError(effect.failure)
            }
        }
    }

    BackHandler(onBack = viewModel::leaveSession)

    // Nothing to shop: the prototype's `go('live')` guard sends you straight back to Belanja.
    LaunchedEffect(state.session, state.loadState) {
        if (state.session != null) return@LaunchedEffect
        if (state.loadState !is UiState.Success) return@LaunchedEffect

        onBack()
    }

    val session = state.session ?: return
    val storeName = session.store.ifBlank { stringResource(R.string.common_session_untitled_store) }

    AppScaffold(
        modifier = modifier,
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
        bottomBar = {
            LiveSessionBottomBar(
                total = state.total,
                onCancel = viewModel::requestCancel,
                onFinish = viewModel::openFinishSheet,
            )
        },
        header = {
            AppScreenHeader(
                title = storeName,
                subtitle = stringResource(
                    R.string.live_header_subtitle,
                    session.startedAt.toTimeLabel(),
                ),
                onBack = viewModel::leaveSession,
                actions = {
                    AppIconButton(
                        onClick = { showStoreSheet = true },
                        contentDescription = stringResource(R.string.common_cd_edit_store),
                        emoji = "✎",
                    )
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = AppTheme.shapes.listPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            item(key = "receipt") {
                ReceiptHeader(
                    label = stringResource(R.string.live_receipt_label),
                    amount = state.total.toRupiah(),
                    footerLeft = pluralStringResource(
                        R.plurals.common_item_count,
                        state.itemCount,
                        state.itemCount,
                    ),
                    footerRight = when (val last = state.lastItemName) {
                        null -> stringResource(R.string.live_receipt_no_items)
                        else -> stringResource(R.string.live_receipt_last, last)
                    },
                )
                Spacer(Modifier.height(Spacing.x8))
            }

            item(key = "addItem") {
                LiveAddItemCard(
                    state = state,
                    form = form,
                    onNameChanged = viewModel::onNameChanged,
                    onPickName = viewModel::pickName,
                    onPickCategory = viewModel::pickCategory,
                    onPickBrand = viewModel::pickBrand,
                    onPickUnit = viewModel::pickUnit,
                    onUseLastPrice = { viewModel.useLastPrice(form.note, form.qty) },
                    isDetailOpen = isDetailOpen,
                    onToggleDetail = { isDetailOpen = !isDetailOpen },
                    onAddItem = {
                        viewModel.addItem(
                            name = state.query,
                            qtyText = form.qty,
                            unit = state.selectedUnit,
                            note = form.note,
                            priceText = form.price.toString(),
                        )
                    },
                )
            }

            if (state.hasList) {
                item(key = "list") {
                    Spacer(Modifier.height(Spacing.x8))
                    LiveListStrip(
                        remaining = state.listRemaining,
                        preview = state.listPreview,
                        hiddenCount = state.listHiddenCount,
                        checkedCount = state.listCheckedCount,
                        totalCount = state.listTotalCount,
                        progress = state.listProgress,
                        isComplete = state.isListComplete,
                        isExpanded = isListExpanded,
                        onToggleExpanded = { isListExpanded = !isListExpanded },
                        onPickName = viewModel::pickName,
                    )
                }
            }

            item(key = "cartHeader") {
                AppSectionHeader(
                    title = stringResource(R.string.live_cart_title),
                    trailing = {
                        Text(
                            text = stringResource(R.string.live_cart_hint),
                            style = AppTheme.typography.tiny,
                        )
                    },
                )
            }

            if (state.itemViews.isEmpty()) {
                item(key = "cartEmpty") { LiveCartEmptyState() }
            }

            items(items = state.itemViews, key = { it.item.id }) { view ->
                // A bought item slides in at the head of the cart and a removed one collapses,
                // the same way a ticked line moves on the Daftar screen.
                LiveCartRow(
                    view = view,
                    onClick = { editingItem = view },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (showStoreSheet) {
        LiveStoreSheet(
            store = session.store,
            onSave = { store ->
                showStoreSheet = false
                viewModel.updateStore(store)
            },
            onDismiss = { showStoreSheet = false },
        )
    }

    val editing = editingItem
    if (editing != null) {
        LiveItemSheet(
            view = editing,
            unit = editing.item.unit ?: state.selectedUnit,
            onSave = { name, qtyText, unit, note, priceText ->
                viewModel.updateItem(
                    itemId = editing.item.id,
                    name = name,
                    qtyText = qtyText,
                    unit = unit,
                    note = note,
                    priceText = priceText,
                )
            },
            onDelete = { viewModel.deleteItem(editing.item.id) },
            onDismiss = { editingItem = null },
        )
    }

    if (showFinishSheet) {
        LiveFinishSheet(
            total = state.total,
            itemCount = state.itemCount,
            stockableCount = state.stockableCount,
            listLeftoverCount = state.listRemainingCount,
            defaultName = session.store.ifBlank {
                stringResource(R.string.live_finish_default_name, state.finishedAtMillis.toDayLabel())
            },
            finishedAt = state.finishedAtMillis,
            onConfirm = viewModel::finishSession,
            onDismiss = { showFinishSheet = false },
        )
    }

    if (!showCancelSheet) return

    ConfirmationBottomSheet(
        title = stringResource(R.string.live_cancel_sheet_title),
        message = pluralStringResource(
            R.plurals.live_cancel_sheet_message,
            state.itemCount,
            state.itemCount,
        ),
        confirmText = stringResource(R.string.live_cancel_confirm),
        onConfirm = viewModel::cancelSession,
        onDismiss = { showCancelSheet = false },
        cancelText = stringResource(R.string.common_back),
        isDanger = true,
    )
}
