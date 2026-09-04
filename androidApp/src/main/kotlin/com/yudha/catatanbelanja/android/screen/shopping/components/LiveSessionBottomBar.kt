package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppRollingText
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiah

/** The pinned `.finish` bar: the running total, "Batal" and "Selesai ✓". */
@Composable
internal fun LiveSessionBottomBar(
    total: Int,
    onCancel: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.shapes.radius))
            .background(colors.ink)
            .padding(start = 18.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.common_total),
                style = AppTheme.typography.tiny,
                color = colors.paper.copy(alpha = 0.7f),
            )
            AppRollingText(
                text = total.toRupiah(),
                style = AppTheme.typography.statValue,
                color = colors.paper,
            )
        }

        AppButton(
            text = stringResource(R.string.common_cancel),
            onClick = onCancel,
            variant = AppButtonVariant.OnHero,
            fillWidth = false,
        )
        AppButton(
            text = stringResource(R.string.live_finish),
            onClick = onFinish,
            emoji = "✓",
            fillWidth = false,
        )
    }
}
