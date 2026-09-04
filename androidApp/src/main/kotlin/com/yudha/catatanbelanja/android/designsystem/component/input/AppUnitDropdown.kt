package com.yudha.catatanbelanja.android.designsystem.component.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.catalog.CatalogData

/** A `.field select` look-alike over the unit catalog. Opens a menu instead of a native picker. */
@Composable
fun AppUnitDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    units: List<String> = CatalogData.units,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor by animateColorAsState(
        targetValue = if (expanded) colors.primary else colors.background,
        label = "unitBorder",
    )
    val fillColor by animateColorAsState(
        targetValue = if (expanded) colors.paper else colors.background,
        label = "unitFill",
    )
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x6),
    ) {
        if (label != null) {
            Text(text = label, style = AppTheme.typography.fieldLabel, color = colors.inkSecondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(fillColor)
                .border(2.dp, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = { expanded = true },
                )
                .padding(start = 14.dp, top = 13.dp, end = 12.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = AppTheme.typography.bodyLarge,
                color = if (enabled) colors.ink else colors.inkTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.paper),
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = unit,
                            style = AppTheme.typography.bodyLarge,
                            color = if (unit == value) colors.primaryDark else colors.ink,
                        )
                    },
                    onClick = {
                        expanded = false
                        onValueChange(unit)
                    },
                )
            }
        }
    }
}
