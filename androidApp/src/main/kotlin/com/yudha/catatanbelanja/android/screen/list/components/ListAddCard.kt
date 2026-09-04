package com.yudha.catatanbelanja.android.screen.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.list.presentation.ShoppingListState

/**
 * One field and a wall of chips. Writing a list is the step people give up on, so the keyboard
 * never has to close: Enter adds and keeps the caret, and every chip is a whole item in one tap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ListAddCard(
    state: ShoppingListState,
    focusRequester: FocusRequester,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onPickName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typed = state.query.trim()
    val isSearching = state.searchChips.isNotEmpty() || state.showNewItemChip

    AppCard(modifier = modifier) {
        AppTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier.focusRequester(focusRequester),
            placeholder = stringResource(R.string.list_add_placeholder),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailing = {
                AppIconButton(
                    onClick = onSubmit,
                    contentDescription = stringResource(R.string.list_add_action),
                    icon = Icons.Rounded.Add,
                    tint = AppTheme.colors.primaryDark,
                    backgroundColor = AppTheme.colors.tint,
                )
            },
        )

        if (isSearching) {
            Spacer(Modifier.height(Spacing.x12))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
                verticalArrangement = Arrangement.spacedBy(Spacing.x8),
            ) {
                state.searchChips.forEach { chip ->
                    AppChip(text = chip.name, onClick = { onPickName(chip.name) }, emoji = chip.emoji)
                }

                if (!state.showNewItemChip) return@FlowRow

                AppChip(
                    text = stringResource(R.string.list_new_item_chip, typed),
                    onClick = { onPickName(typed) },
                    emoji = "＋",
                    variant = AppChipVariant.Dark,
                )
            }
            return@AppCard
        }

        if (state.quickAddChips.isEmpty()) return@AppCard

        Spacer(Modifier.height(Spacing.x12))
        Text(text = stringResource(R.string.list_quick_add_title), style = AppTheme.typography.fieldLabel)
        Spacer(Modifier.height(Spacing.x8))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            state.quickAddChips.forEach { chip ->
                AppChip(text = chip.name, onClick = { onPickName(chip.name) }, emoji = chip.emoji)
            }
        }
    }
}
