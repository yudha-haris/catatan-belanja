package com.yudha.catatanbelanja.android.designsystem.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

/**
 * The `.card` surface. [flat] drops the shadow and draws a 1.5dp [borderColor] outline instead,
 * which is how the prototype tints a card (low-stock uses the coral border).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    flat: Boolean = false,
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppTheme.shapes.radius)
    val outline = borderColor ?: AppTheme.colors.line
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (flat) Modifier else Modifier.appShadow(shape),
            )
            .clip(shape)
            .background(AppTheme.colors.paper)
            .then(if (flat) Modifier.border(1.5.dp, outline, shape) else Modifier)
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                },
            )
            .padding(contentPadding),
        content = content,
    )
}
