package com.yudha.catatanbelanja.android.designsystem.component.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** Max digits that still fit an Int rupiah amount. */
private const val MAX_DIGITS = 9

/**
 * The `.money` field: a fixed "Rp" prefix and a big tabular amount that regroups with Indonesian
 * thousands dots on every keystroke. [onValueChange] emits the raw rupiah amount.
 */
@Composable
fun AppMoneyField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0",
    prefix: String = stringResource(R.string.common_currency_prefix),
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = AppTheme.colors
    var focused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.primary else colors.background,
        label = "moneyBorder",
    )
    val fillColor by animateColorAsState(
        targetValue = if (focused) colors.paper else colors.background,
        label = "moneyFill",
    )
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)
    val amountStyle = AppTheme.typography.moneyInput

    val formatted = if (value <= 0) "" else groupThousands(value.toString())
    val fieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x6),
    ) {
        if (label != null) {
            Text(text = label, style = AppTheme.typography.fieldLabel, color = colors.inkSecondary)
        }

        BasicTextField(
            value = fieldValue,
            onValueChange = { input ->
                val digits = input.text.filter(Char::isDigit).trimStart('0').take(MAX_DIGITS)
                onValueChange(digits.toIntOrNull() ?: 0)
            },
            enabled = enabled,
            singleLine = true,
            textStyle = amountStyle.copy(color = if (enabled) colors.ink else colors.inkTertiary),
            cursorBrush = SolidColor(colors.primary),
            // Spelled out rather than left to the platform default, so [keyboardActions]'s
            // `onDone` is guaranteed to fire — the amount is always the last thing typed.
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = prefix,
                        style = AppTheme.typography.rowTitle,
                        color = colors.inkSecondary,
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (formatted.isEmpty()) {
                            Text(text = placeholder, style = amountStyle, color = colors.inkTertiary)
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}

/** "1234567" -> "1.234.567". */
private fun groupThousands(digits: String): String {
    if (digits.length <= 3) return digits
    val grouped = StringBuilder()
    digits.forEachIndexed { index, char ->
        if (index > 0 && (digits.length - index) % 3 == 0) grouped.append('.')
        grouped.append(char)
    }
    return grouped.toString()
}
