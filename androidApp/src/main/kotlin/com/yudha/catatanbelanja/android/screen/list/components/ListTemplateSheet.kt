package com.yudha.catatanbelanja.android.screen.list.components

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

/** Names the template. The only thing this feature ever asks the user to type twice. */
@Composable
internal fun ListTemplateSheet(
    defaultName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(defaultName) { mutableStateOf(defaultName) }

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.list_template_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.list_template_name_label),
            placeholder = stringResource(R.string.list_template_name_placeholder),
        )
        Spacer(Modifier.height(Spacing.x16))

        AppButton(
            text = stringResource(R.string.list_template_save),
            onClick = { onSave(name) },
            enabled = name.isNotBlank(),
            big = true,
        )
    }
}
