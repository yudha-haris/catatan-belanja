package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppTextField
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** Renaming the shop mid-session — the prototype's `editStore` sheet. */
@Composable
internal fun LiveStoreSheet(
    store: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(store) { mutableStateOf(store) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.live_store_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))
        AppTextField(
            value = value,
            onValueChange = { value = it },
            placeholder = stringResource(R.string.common_store_name_placeholder),
        )
        Spacer(Modifier.height(Spacing.x16))
        AppButton(
            text = stringResource(R.string.common_save),
            onClick = { onSave(value) },
            big = true,
        )
    }
}
