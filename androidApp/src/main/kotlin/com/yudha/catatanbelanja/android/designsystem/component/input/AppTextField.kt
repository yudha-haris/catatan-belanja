package com.yudha.catatanbelanja.android.designsystem.component.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The `.field` input: a 12sp label with an optional lighter [optionalLabel] suffix over a filled
 * box that swaps to a paper background and a primary border while focused.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    optionalLabel: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    textStyle: TextStyle = AppTheme.typography.bodyLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    var focused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.primary else colors.background,
        label = "fieldBorder",
    )
    val fillColor by animateColorAsState(
        targetValue = if (focused) colors.paper else colors.background,
        label = "fieldFill",
    )
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x6),
    ) {
        if (label != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x4)) {
                Text(text = label, style = AppTheme.typography.fieldLabel, color = colors.inkSecondary)
                if (optionalLabel != null) {
                    Text(text = optionalLabel, style = AppTheme.typography.tiny, color = colors.inkTertiary)
                }
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = textStyle.copy(color = if (enabled) colors.ink else colors.inkTertiary),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(fillColor)
                        .border(2.dp, borderColor, shape)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leading != null) leading()
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (placeholder != null && value.isEmpty()) {
                            Text(text = placeholder, style = textStyle, color = colors.inkTertiary, maxLines = 1)
                        }
                        innerTextField()
                    }
                    if (trailing != null) trailing()
                }
            },
        )
    }
}
