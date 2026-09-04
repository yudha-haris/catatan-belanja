package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.input.AppMoneyField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppSearchField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.input.AppUnitDropdown
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.shopping.LiveAddForm
import com.yudha.catatanbelanja.features.shopping.presentation.LiveSessionState

/**
 * The `#addCard` form. Enter walks the prototype's path — nama → harga, jumlah → merk,
 * merk → harga, harga → tambah — so a whole item can be logged without leaving the keyboard.
 *
 * Two inputs are on show: what, and how much. Jumlah, satuan and merk are all optional, and five
 * fields stacked under a receipt is the kind of form people stop filling in, so they fold behind
 * one chip. The chip stays open once opened — someone who wants quantities wants them all trip.
 *
 * Harga and the button are never folded away: both carry a [FocusRequester], and the flow points
 * at them from the moment a name is picked.
 *
 * Picking a name jumps focus to Harga, which is usually below the fold. The field scrolls itself
 * into view — see `AppScaffold`'s `imePadding`, without which there is nowhere to scroll to — and
 * the keyboard's own Done adds the item, so the flow ends without reaching for the button.
 */
@Composable
internal fun LiveAddItemCard(
    state: LiveSessionState,
    form: LiveAddForm,
    onNameChanged: (String) -> Unit,
    onPickName: (String) -> Unit,
    onPickCategory: (String) -> Unit,
    onPickBrand: (String) -> Unit,
    onPickUnit: (String) -> Unit,
    onUseLastPrice: () -> Unit,
    onAddItem: () -> Unit,
    isDetailOpen: Boolean,
    onToggleDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val browsing = !state.isNamePicked && state.query.isBlank()
    val searching = !state.isNamePicked && state.query.isNotBlank()

    AppCard(modifier = modifier) {
        AppSearchField(
            value = state.query,
            onValueChange = onNameChanged,
            onClear = { onNameChanged("") },
            modifier = Modifier
                .focusRequester(form.nameFocus)
                .shake(form.nameShake),
            placeholder = stringResource(R.string.live_search_placeholder),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { form.priceFocus.requestFocus() }),
        )

        if (browsing) {
            Spacer(Modifier.height(Spacing.x10))
            LiveBrowseSuggestions(
                frequentNames = state.frequentNames,
                selectedCategory = state.selectedCategory,
                categoryItems = state.categoryItems,
                onPickName = onPickName,
                onPickCategory = onPickCategory,
            )
        }

        if (searching) {
            Spacer(Modifier.height(Spacing.x10))
            LiveNameSuggestions(
                query = state.query,
                suggestions = state.nameSuggestions,
                showNewItemChip = state.showNewItemChip,
                onPickName = onPickName,
            )
        }

        val lastPurchase = state.lastPurchase
        if (lastPurchase != null) {
            Spacer(Modifier.height(Spacing.x10))
            LiveLastPriceHint(lastPurchase = lastPurchase, onUseLastPrice = onUseLastPrice)
        }

        Spacer(Modifier.height(Spacing.x12))
        AppChip(
            text = stringResource(R.string.live_detail_toggle),
            onClick = onToggleDetail,
            emoji = if (isDetailOpen) "−" else "＋",
            selected = isDetailOpen,
            variant = AppChipVariant.Plain,
        )

        AnimatedVisibility(
            visible = isDetailOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Spacer(Modifier.height(Spacing.x12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
                ) {
                    AppTextField(
                        value = form.qty,
                        onValueChange = { form.qty = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(form.qtyFocus),
                        label = stringResource(R.string.common_qty_label),
                        placeholder = stringResource(R.string.live_qty_placeholder),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = { form.noteFocus.requestFocus() }),
                    )
                    AppUnitDropdown(
                        value = state.selectedUnit,
                        onValueChange = onPickUnit,
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.common_unit_label),
                    )
                }

                Spacer(Modifier.height(Spacing.x12))
                AppTextField(
                    value = form.note,
                    onValueChange = { form.note = it },
                    modifier = Modifier.focusRequester(form.noteFocus),
                    label = stringResource(R.string.live_brand_label),
                    placeholder = stringResource(R.string.live_brand_placeholder),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { form.priceFocus.requestFocus() }),
                )

                if (state.brandSuggestions.isEmpty()) return@Column

                Spacer(Modifier.height(Spacing.x8))
                LiveBrandSuggestions(brands = state.brandSuggestions, onPickBrand = onPickBrand)
            }
        }

        Spacer(Modifier.height(Spacing.x12))
        AppMoneyField(
            value = form.price,
            onValueChange = { form.price = it },
            modifier = Modifier
                .focusRequester(form.priceFocus)
                .shake(form.priceShake),
            label = stringResource(R.string.common_price_label),
            placeholder = stringResource(R.string.live_price_placeholder),
            keyboardActions = KeyboardActions(onDone = { onAddItem() }),
        )

        Spacer(Modifier.height(Spacing.x14))
        AppButton(
            text = stringResource(R.string.live_add_to_cart),
            onClick = onAddItem,
            emoji = "＋",
            big = true,
        )
    }
}
