package com.yudha.catatanbelanja.android.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

/** The prototype's four tab glyphs and labels, in tab-index order: cart, clock, box, bars. */
private val shellTabs: List<Pair<ImageVector, Int>> = listOf(
    Icons.Rounded.ShoppingCart to R.string.common_tab_shopping,
    Icons.Rounded.Schedule to R.string.common_tab_history,
    Icons.Rounded.Inventory2 to R.string.common_tab_stock,
    Icons.Rounded.BarChart to R.string.common_tab_dashboard,
)

/** The prototype's `.tabbar`: a paper pill floating above the navigation bar. */
@Composable
fun ShellTabBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    stockBadgeCount: Int = 0,
) {
    val barShape = RoundedCornerShape(24.dp)
    val tabShape = RoundedCornerShape(AppTheme.shapes.radiusItem)
    val gutter = Spacing.x16

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = gutter, end = gutter, bottom = Spacing.x12),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = AppTheme.shapes.maxContentWidth - gutter - gutter)
                .fillMaxWidth()
                .appShadow(shape = barShape, spec = AppTheme.shapes.tabBarShadow)
                .clip(barShape)
                .background(AppTheme.colors.paper)
                .padding(Spacing.x8),
        ) {
            shellTabs.forEachIndexed { index, (icon, labelRes) ->
                val selected = index == selectedIndex
                val backgroundColor by animateColorAsState(
                    targetValue = when (selected) {
                        true -> AppTheme.colors.tint
                        false -> AppTheme.colors.paper
                    },
                    label = "shellTabBackground",
                )
                val contentColor by animateColorAsState(
                    targetValue = when (selected) {
                        true -> AppTheme.colors.primaryDark
                        false -> AppTheme.colors.inkTertiary
                    },
                    label = "shellTabContent",
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(tabShape)
                        .background(backgroundColor)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        )
                        .padding(vertical = Spacing.x8, horizontal = Spacing.x4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp),
                        )

                        // How many things need buying, carried on the tab itself so the home
                        // screen does not have to spend a whole card saying it.
                        if (index == TAB_STOCK && stockBadgeCount > 0) {
                            val badgeLabel = pluralStringResource(
                                R.plurals.common_tab_stock_badge,
                                stockBadgeCount,
                                stockBadgeCount,
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 9.dp, y = (-6).dp)
                                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.coral)
                                    .padding(horizontal = 4.dp)
                                    .semantics { contentDescription = badgeLabel },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stockBadgeCount.toString(),
                                    style = AppTheme.typography.tabLabel,
                                    color = AppTheme.colors.paper,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(labelRes),
                        style = AppTheme.typography.tabLabel,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
