package com.yudha.catatanbelanja.android.designsystem.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/** The `.search` field: leading magnifier, and a round clear pill that only appears once [value] is non-empty. */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    searchContentDescription: String? = stringResource(R.string.common_cd_search),
    clearContentDescription: String? = stringResource(R.string.common_cd_clear_text),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = AppTheme.colors
    val clearInteractionSource = remember { MutableInteractionSource() }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        textStyle = AppTheme.typography.searchInput,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leading = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = searchContentDescription,
                tint = colors.inkTertiary,
                modifier = Modifier.size(20.dp),
            )
        },
        trailing = trailing@{
            if (value.isEmpty()) return@trailing

            // The pill stays 32dp; only its hit area grows to the 48dp minimum.
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.line)
                    .clickable(
                        interactionSource = clearInteractionSource,
                        indication = null,
                        onClick = onClear,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = clearContentDescription,
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )
}
