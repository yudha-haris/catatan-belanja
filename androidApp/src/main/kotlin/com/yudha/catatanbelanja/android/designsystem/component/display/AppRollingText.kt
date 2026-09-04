package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow

/**
 * A number that rolls rather than cuts. Watching a running total climb is most of what makes
 * logging a trip feel like progress instead of paperwork, so every figure that changes while the
 * user is looking at it goes through here.
 *
 * Always upward: a shopping total only really goes one way, and a direction that guessed from the
 * value would flip on the rare correction and read as a glitch.
 */
@Composable
fun AppRollingText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = {
            val enter = slideInVertically { height -> height } + fadeIn()
            val exit = slideOutVertically { height -> -height } + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        },
        label = "rollingText",
    ) { value ->
        Text(
            text = value,
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
