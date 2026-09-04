package com.yudha.catatanbelanja.android.designsystem.component.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

/**
 * The 40dp `.back` pill: paper background, radius 14, soft shadow. Renders [icon] or [emoji].
 * The label sits on the container, not on the `Icon`, so the emoji path announces it too.
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    tint: Color = AppTheme.colors.ink,
    backgroundColor: Color = AppTheme.colors.paper,
) {
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "iconButtonPress")
    val label = contentDescription.orEmpty()

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .appShadow(shape)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                this.contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
        if (emoji == null) return@Box

        Text(
            text = emoji,
            style = AppTheme.typography.emoji,
            color = tint,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
