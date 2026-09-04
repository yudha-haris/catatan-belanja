package com.yudha.catatanbelanja.android.screen.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * The tick box of a list line. Ticking it is the whole reward loop of the feature, so it is not
 * a plain checkbox: the ring fills, the mark springs in, and the circle overshoots on its way.
 */
@Composable
internal fun ListCheckCircle(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val container by animateColorAsState(
        targetValue = if (isChecked) colors.primary else colors.background,
        label = "listCheckContainer",
    )
    val ring by animateColorAsState(
        targetValue = if (isChecked) colors.primary else colors.line,
        label = "listCheckRing",
    )
    val pop by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.42f, stiffness = 620f),
        label = "listCheckPop",
    )

    Box(
        modifier = modifier
            .size(26.dp)
            .scale(pop)
            .clip(CircleShape)
            .background(container)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isChecked,
            enter = scaleIn(spring(dampingRatio = 0.4f, stiffness = 700f)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.paper,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
