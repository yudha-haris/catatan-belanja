package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The store field as it looks on the hero gradient: `AppTextField`'s paper fill would fight the
 * gradient, so this is the prototype's translucent white variant of the same box.
 */
@Composable
internal fun StartStoreField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var focused by remember { mutableStateOf(false) }
    val fillAlpha by animateFloatAsState(
        targetValue = if (focused) 0.28f else 0.18f,
        label = "storeFieldFill",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (focused) 0.6f else 0f,
        label = "storeFieldBorder",
    )
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x6),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.x4)) {
            Text(
                text = stringResource(R.string.common_store_name_label),
                style = AppTheme.typography.fieldLabel,
                color = colors.paper.copy(alpha = 0.85f),
            )
            Text(
                text = stringResource(R.string.common_optional),
                style = AppTheme.typography.tiny,
                color = colors.paper.copy(alpha = 0.7f),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppTheme.typography.bodyLarge.copy(color = colors.paper),
            cursorBrush = SolidColor(colors.paper),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colors.paper.copy(alpha = fillAlpha))
                        .border(2.dp, colors.paper.copy(alpha = borderAlpha), shape)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.common_store_name_placeholder),
                            style = AppTheme.typography.bodyLarge,
                            color = colors.paper.copy(alpha = 0.7f),
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
