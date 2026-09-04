package com.yudha.catatanbelanja.android.screen.list.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.list.domain.model.ShoppingListItemView

/**
 * One line of the plan. The whole row is the tick target — a 26dp checkbox is a bad thing to aim
 * at while pushing a trolley — and the ✕ is the only other thing on it.
 */
@Composable
internal fun ListItemRow(
    view: ShoppingListItemView,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusItem)
    val interactionSource = remember { MutableInteractionSource() }
    val isChecked = view.item.isChecked
    val titleColor by animateColorAsState(
        targetValue = if (isChecked) colors.inkTertiary else colors.ink,
        label = "listItemTitle",
    )
    val toggleLabel = when (isChecked) {
        true -> stringResource(R.string.list_item_uncheck_cd, view.item.name)
        false -> stringResource(R.string.list_item_check_cd, view.item.name)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.paper)
            .border(1.5.dp, colors.line, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onClickLabel = toggleLabel,
                onClick = onToggle,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ListCheckCircle(isChecked = isChecked)

        Text(text = view.emoji, style = AppTheme.typography.emoji)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = view.item.name,
                style = AppTheme.typography.rowTitle,
                color = titleColor,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (view.item.note.isEmpty()) return@Column

            Text(
                text = view.item.note,
                style = AppTheme.typography.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(AppTheme.shapes.radiusSmall))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.list_item_remove_cd, view.item.name),
                tint = colors.inkTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
